package lol.terabrendon.houseshare2.data.repository

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.coroutines.coroutineBinding
import com.github.michaelbull.result.getOrElse
import com.github.michaelbull.result.getOrThrow
import com.github.michaelbull.result.onFailure
import com.github.michaelbull.result.onSuccess
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import lol.terabrendon.houseshare2.data.local.dao.GroupDao
import lol.terabrendon.houseshare2.data.local.dao.UserDao
import lol.terabrendon.houseshare2.data.local.util.localSafe
import lol.terabrendon.houseshare2.data.remote.api.UserApi
import lol.terabrendon.houseshare2.data.util.DataResult
import lol.terabrendon.houseshare2.di.IoDispatcher
import lol.terabrendon.houseshare2.domain.error.RootException
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
    @param:IoDispatcher
    private val io: CoroutineDispatcher,
    private val userDataRepository: UserDataRepository,
    private val groupDao: GroupDao,
) : UserRepository {
    // TODO: consider something else?
    private val refreshGroups = AtomicBoolean(false)
    private val refreshUsers = AtomicBoolean(false)

    private suspend fun refreshUserGroups(userId: Long): DataResult<Unit> {
        Timber.i("refreshUserGroups: refreshing groups for userId=${userId}")

        val groups = userApi.getGroups(userId).getOrElse { return Err(it) }

        coroutineBinding {
            // Add all groups
            groups.map { group -> launch { groupDao.upsert(group.toEntity()) } }.joinAll()

            val missingUsers = groups
                .flatMap { group -> group.userIds.map { group.id to it } }
                .asFlow()
                .filter { (_, userId) -> !userDao.existById(userId) }
                .toList()
                .distinctBy { (_, userId) -> userId }

            // Get missing users
            missingUsers.map { (groupId, userId) ->
                launch { refreshGroupUser(groupId, userId).bind() }
            }.joinAll()

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
                .map { launch { groupDao.delete(it) } }
                .joinAll()

            // Refresh groups
            groups
                .map { it.toEntity() to it.userIds }
                .map { launch { groupDao.upsertGroup(it.first, it.second) } }
                .joinAll()

            groups
        }
            .onSuccess { groups ->
                Timber.i(
                    "refreshUserGroups: completed successfully, groupIds=%s",
                    groups.map { it.id })
            }
            .onFailure { Timber.w("refreshUserGroups: refreshFailed: err=%s", it) }
            .getOrElse { return Err(it) }

        return Ok(Unit)
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
            externalScope.launch(io) {
                refreshUserGroups(userId).getOrThrow { RootException(it) }
            }
        }

        return userDao
            .findGroupsByUserId(userId)
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

    override suspend fun refreshUsers(): DataResult<Unit> {
        Timber.i("refreshUsers: refreshing all visible users")

        val loggedUserId = userDataRepository.currentLoggedUserId.filterNotNull().first()

        return coroutineBinding {
            val groups = userApi.getGroups(loggedUserId).bind()

            val usersToUpsert = groups
                .flatMap { group -> group.userIds.map { group.id to it } }
                .distinctBy { (_, userId) -> userId }
                .map { (groupId, userId) ->
                    async {
                        userApi.getGroupUser(groupId, userId).bind()
                    }
                }
                .awaitAll()

            localSafe { userDao.upsertAll(usersToUpsert.map { it.toEntity() }) }.bind()

            Unit
        }
            .onFailure { Timber.w("refreshUsers: refresh failed, err=%s", it) }
    }

    override suspend fun refreshGroupUsers(groupId: Long): DataResult<Unit> {
        val users = userApi.getGroupUsers(groupId).getOrElse { return Err(it) }

        localSafe { userDao.upsertAll(users.map { it.toEntity() }) }.getOrElse { return Err(it) }

        Timber.i("refreshGroupUsers: refreshed users of groupId=%d", groupId)

        return Ok(Unit)
    }

    override suspend fun refreshGroupUser(groupId: Long, userId: Long): DataResult<Unit> {
        val userDto = userApi.getGroupUser(groupId, userId).getOrElse { return Err(it) }

        localSafe { userDao.upsert(userDto.toEntity()) }.getOrElse { return Err(it) }

        Timber.i("refreshGroupUser: refreshed userId=%d of groupId=%d", userId, groupId)

        return Ok(Unit)
    }
}