package lol.terabrendon.houseshare2.data.repository

import com.github.michaelbull.result.coroutines.coroutineBinding
import com.github.michaelbull.result.onFailure
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import lol.terabrendon.houseshare2.data.local.dao.UserDao
import lol.terabrendon.houseshare2.data.local.util.localSafe
import lol.terabrendon.houseshare2.data.remote.api.GroupApi
import lol.terabrendon.houseshare2.data.remote.api.UserApi
import lol.terabrendon.houseshare2.data.util.DataResult
import lol.terabrendon.houseshare2.domain.mapper.toEntity
import lol.terabrendon.houseshare2.domain.mapper.toModel
import lol.terabrendon.houseshare2.domain.model.UserModel
import timber.log.Timber
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val userDao: UserDao,
    private val userApi: UserApi,
    private val groupApi: GroupApi,
    private val externalScope: CoroutineScope,
    private val userDataRepository: UserDataRepository,
) : UserRepository {
    // TODO: consider something else?
    private val refreshUsers = AtomicBoolean(false)


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
                        groupApi.getGroupUser(groupId, userId).bind()
                    }
                }
                .awaitAll()

            localSafe { userDao.upsertAll(usersToUpsert.map { it.toEntity() }) }.bind()

            Unit
        }
            .onFailure { Timber.w("refreshUsers: refresh failed, err=%s", it) }
    }
}