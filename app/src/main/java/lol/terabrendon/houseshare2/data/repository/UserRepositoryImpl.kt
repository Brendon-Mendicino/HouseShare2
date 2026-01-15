package lol.terabrendon.houseshare2.data.repository

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import lol.terabrendon.houseshare2.data.local.dao.GroupDao
import lol.terabrendon.houseshare2.data.local.dao.UserDao
import lol.terabrendon.houseshare2.data.remote.api.UserApi
import lol.terabrendon.houseshare2.di.IoDispatcher
import lol.terabrendon.houseshare2.domain.mapper.toEntity
import lol.terabrendon.houseshare2.domain.mapper.toModel
import lol.terabrendon.houseshare2.domain.model.GroupInfoModel
import lol.terabrendon.houseshare2.domain.model.UserModel
import timber.log.Timber
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val userDao: UserDao,
    private val userApi: UserApi,
    private val externalScope: CoroutineScope,
    @IoDispatcher
    private val ioDispatcher: CoroutineDispatcher,
    private val userDataRepository: UserDataRepository,
    private val groupDao: GroupDao,
) : UserRepository {
    // TODO: consider something else?
    private val refreshGroups = AtomicBoolean(false)
    private val refreshUsers = AtomicBoolean(false)

    private fun refreshUserGroups(userId: Long) {
        externalScope.launch(ioDispatcher) {
            Timber.i("refreshUserGroups: refreshing groups for userId=${userId}")

            val groups = userApi.getGroups(userId)
            val usersToRequest =
                groups.flatMap { group -> group.userIds.map { group.id to it } }
                    .filter { (_, userId) -> !userDao.existById(userId) }
                    .distinctBy { (_, userId) -> userId }

            // Get missing users from DB
            usersToRequest.map { (groupId, userId) -> launch { refreshGroupUser(groupId, userId) } }
                .joinAll()

            val groupIds = groups.map { it.id }.toSet()

            // Remove groups that are no more present
            userDao
                .findGroupsByUserId(userId)
                .first()
                ?.groups
                .let { it ?: emptyList() }
                .map { it.id }
                .filter { it !in groupIds }
                .distinct()
                .map { async { groupDao.delete(it) } }
                .joinAll()

            // Refresh groups
            groups
                .map { it.toEntity() to it.userIds }
                .map { async { groupDao.upsertGroup(it.first, it.second) } }
                .joinAll()
        }
    }

    override fun findAll(): Flow<List<UserModel>> {
        if (refreshUsers.compareAndSet(false, true)) {
            externalScope.launch {
                refreshUsers()
            }
        }

        return userDao.findAll().map { users -> users.map { it.toModel() } }
    }

    override fun findById(id: Long): Flow<UserModel?> =
        userDao.findById(id).distinctUntilChanged().map { it?.toModel() }

    override fun findAllById(ids: List<Long>): Flow<List<UserModel>> =
        userDao.findAllById(ids).map { users -> users.map { it.toModel() } }

    override fun findGroupsByUserId(userId: Long): Flow<List<GroupInfoModel>> {
        if (refreshGroups.compareAndSet(false, true)) {
            refreshUserGroups(userId)
        }

        return userDao
            .findGroupsByUserId(userId)
            .distinctUntilChanged()
            .map { groups ->
                groups?.groups?.map {
                    GroupInfoModel(
                        groupId = it.id,
                        name = it.name,
                        description = it.description,
                    )
                } ?: emptyList()
            }
    }

    override suspend fun refreshUsers() {
        externalScope.launch(ioDispatcher) {
            Timber.i("refreshUsers: refreshing all visible users")

            val loggedUserId = userDataRepository.currentLoggedUserId.filterNotNull().first()

            val groups = userApi.getGroups(loggedUserId)
            val usersToUpsert = groups
                .flatMap { group -> group.userIds.map { group.id to it } }
                .distinctBy { (_, userId) -> userId }
                .map { (groupId, userId) -> async { userApi.getGroupUser(groupId, userId) } }
                .awaitAll()

            userDao.upsertAll(usersToUpsert.map { it.toEntity() })
        }.join()
    }


    override suspend fun refreshGroupUsers(groupId: Long) {
        externalScope.launch(ioDispatcher) {
            Timber.i("refreshGroupUsers: refreshing users of groupId=$groupId")

            val users = userApi.getGroupUsers(groupId)

            userDao.upsertAll(users.map { it.toEntity() })
        }.join()
    }

    override suspend fun refreshGroupUser(groupId: Long, userId: Long) {
        externalScope.launch(ioDispatcher) {
            Timber.i("refreshGroupUser: refreshing userId=$userId of groupId=$groupId")

            val userDto = userApi.getGroupUser(groupId, userId)

            userDao.upsert(userDto.toEntity())
        }.join()
    }
}