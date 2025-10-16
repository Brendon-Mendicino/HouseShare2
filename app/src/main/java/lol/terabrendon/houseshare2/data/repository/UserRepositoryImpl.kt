package lol.terabrendon.houseshare2.data.repository

import android.util.Log
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import lol.terabrendon.houseshare2.data.api.UserApi
import lol.terabrendon.houseshare2.data.dao.GroupDao
import lol.terabrendon.houseshare2.data.dao.UserDao
import lol.terabrendon.houseshare2.data.dto.GroupDto
import lol.terabrendon.houseshare2.data.dto.UserDto
import lol.terabrendon.houseshare2.data.entity.Group
import lol.terabrendon.houseshare2.data.entity.User
import lol.terabrendon.houseshare2.di.IoDispatcher
import lol.terabrendon.houseshare2.domain.mapper.Mapper
import lol.terabrendon.houseshare2.domain.model.GroupInfoModel
import lol.terabrendon.houseshare2.domain.model.UserModel
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val userDao: UserDao,
    private val userApi: UserApi,
    private val externalScope: CoroutineScope,
    @IoDispatcher
    private val ioDispatcher: CoroutineDispatcher,
    private val userDtoToEntity: Mapper<UserDto, User>,
    private val groupDtoToEntity: Mapper<GroupDto, Group>,
    // TODO: REMOVE
    private val sharedPreferencesRepository: UserPreferencesRepository,
    private val groupDao: GroupDao,
) : UserRepository {
    companion object {
        private const val TAG = "UserRepository"
    }

    init {
        // TODO: REMOVE THIS ASAP
        externalScope.launch {
            userApi.getUsers().content.firstOrNull()?.let {
                // TODO: kotlinx.coroutines.stacktrace.recovery
                println("Setting current user $it")
                userDao.upsert(userDtoToEntity.map(it))

                if (sharedPreferencesRepository.currentLoggedUserId.first() == null)
                    sharedPreferencesRepository.updateCurrentLoggedUser(it.id)
            }
        }
    }

    // TODO: consider something else?
    private val refreshGroups = AtomicBoolean(false)
    private val refreshUsers = AtomicBoolean(false)

    private suspend fun refreshUser(userId: Long) {
        Log.i(TAG, "refreshUser: refreshing user id=$userId")

        val userDto = userApi.getUser(userId)
        val daoId = userDao.upsert(userDtoToEntity.map(userDto))

//        assert(userDto.id == daoId) { "Refreshed id is different from saved one in the DB. dtoId=${userDto.id} daoId=${daoId}" }
    }

    private suspend fun CoroutineScope.refreshUsers() {
        Log.i(TAG, "refreshUsers: refresh users")

        val usersDto = userApi.getUsers()
        usersDto.content.map { async { userDao.upsert(userDtoToEntity.map(it)) } }.joinAll()
    }

    fun refreshUserGroups(userId: Long) {
        externalScope.launch(ioDispatcher) {
            Log.i(TAG, "refreshUserGroups: refreshing groups for userId=${userId}")

            val groups = userApi.getGroups(userId)
            val usersToRequest =
                groups.flatMap { it.userIds }.filter { !userDao.existById(it) }.toSet()

            // Get missing users from DB
            usersToRequest.map { async { refreshUser(it) } }.joinAll()

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
                .map { Pair(groupDtoToEntity.map(it), it.userIds) }
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

        return userDao.findAll().map { users -> users.map { UserModel.from(it) } }
    }

    override fun findById(id: Long): Flow<UserModel?> =
        userDao.findById(id).map { it?.let { UserModel.from(it) } }

    override fun findAllById(ids: List<Long>): Flow<List<UserModel>> =
        userDao.findAllById(ids).map { users -> users.map { UserModel.from(it) } }

    override suspend fun insert(user: UserModel) = externalScope.launch {
        val dto = userApi.save(UserDto(id = user.id, username = user.username))

        val entity = User.from(user)
        val newId = userDao.insert(entity.copy(id = dto.id))

        assert(dto.id == newId) { "Refreshed id is different from saved one in the DB. dtoId=${dto.id} daoId=${newId}" }

        Log.i(TAG, "insert: added new user: ${entity.copy(id = newId)}")
    }.join()

    override fun findGroupsByUserId(userId: Long): Flow<List<GroupInfoModel>> {
        if (refreshGroups.compareAndSet(false, true)) {
            refreshUserGroups(userId)
        }

        return userDao
            .findGroupsByUserId(userId)
            .map { groups ->
                groups?.groups?.map {
                    GroupInfoModel(
                        groupId = it.id,
                        name = it.name,
                        description = it.description
                    )
                } ?: emptyList()
            }
    }
}