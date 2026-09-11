package lol.terabrendon.houseshare2.data.repository

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.filterErrors
import com.github.michaelbull.result.getOrElse
import com.github.michaelbull.result.getOrThrow
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import lol.terabrendon.houseshare2.data.local.dao.GroupDao
import lol.terabrendon.houseshare2.data.local.dao.GroupMemberDao
import lol.terabrendon.houseshare2.data.local.dao.UserDao
import lol.terabrendon.houseshare2.data.local.util.LocalResult
import lol.terabrendon.houseshare2.data.local.util.localSafe
import lol.terabrendon.houseshare2.data.remote.api.GroupApi
import lol.terabrendon.houseshare2.data.remote.api.UserApi
import lol.terabrendon.houseshare2.data.remote.dto.AppGroupDto
import lol.terabrendon.houseshare2.data.util.DataResult
import lol.terabrendon.houseshare2.di.IoDispatcher
import lol.terabrendon.houseshare2.domain.error.RemoteError
import lol.terabrendon.houseshare2.domain.error.RootException
import lol.terabrendon.houseshare2.domain.mapper.toDto
import lol.terabrendon.houseshare2.domain.mapper.toEntity
import lol.terabrendon.houseshare2.domain.mapper.toModel
import lol.terabrendon.houseshare2.domain.model.GroupInfoModel
import lol.terabrendon.houseshare2.domain.model.GroupMemberModel
import lol.terabrendon.houseshare2.domain.model.GroupModel
import lol.terabrendon.houseshare2.domain.model.UserModel
import timber.log.Timber
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

class GroupRepositoryImpl @Inject constructor(
    private val groupDao: GroupDao,
    private val userDao: UserDao,
    private val groupMemberDao: GroupMemberDao,
    private val groupApi: GroupApi,
    private val userApi: UserApi,
    private val externalScope: CoroutineScope,
    @param:IoDispatcher
    private val io: CoroutineDispatcher,
) : GroupRepository {
    // TODO: consider something else?
    private val refreshGroups = AtomicBoolean(false)
    private val refreshUsers = AtomicBoolean(false)

    private suspend fun upsertDto(dto: AppGroupDto): LocalResult<Unit> {
        val members = dto.members.map { it.toEntity() }
        val users = dto.users.map { it.toEntity() }
        val group = dto.toEntity()

        return localSafe {
            val userIds = userDao.upsertAll(users)
            groupDao.upsertGroup(group, userIds.toList())
            groupMemberDao.upsertAll(members)

            Unit
        }
    }

    override fun findById(groupId: Long): Flow<GroupModel?> =
        groupDao.findById(groupId).distinctUntilChanged().map { group -> group?.toModel() }

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
                    it.toModel()
                } ?: emptyList()
            }
    }

    override suspend fun findOrFetchUser(groupId: Long, userId: Long): DataResult<UserModel?> {
        val user = userDao.findByIdAndGroupId(groupId, userId)
        if (user != null) return Ok(user.toModel())

        val userDto = groupApi.getGroupUser(groupId, userId).getOrElse { err ->
            when (err) {
                is RemoteError.NotFound -> null
                else -> return Err(err)
            }
        }

        if (userDto == null) return Ok(null)

        localSafe { userDao.upsert(userDto.toEntity()) }.getOrElse { return Err(it) }

        Timber.i("refreshGroupUser: refreshed userId=%d of groupId=%d", userId, groupId)

        return Ok(userDto.toModel())
    }

    override suspend fun findOrFetchMember(
        groupId: Long,
        memberId: Long,
    ): DataResult<GroupMemberModel?> {
        val member = groupMemberDao.findByIdAndGroupId(memberId, groupId)

        if (member != null) return Ok(member.toModel())

        // Try and to fetch the member from the api
        val memberDto = groupApi.getGroupMember(groupId, memberId).getOrElse { err ->
            when (err) {
                is RemoteError.NotFound -> null
                else -> return Err(err)
            }
        }

        if (memberDto == null) return Ok(null)

        localSafe { groupMemberDao.upsert(memberDto.toEntity()) }.getOrElse { return Err(it) }

        Timber.i("refreshed memberId=%d of groupId=%d", memberId, groupId)

        return Ok(memberDto.toModel())
    }


    override suspend fun insert(group: GroupModel): DataResult<Unit> {
        val groupDto = groupApi.save(group.toDto()).getOrElse { return Err(it) }

        upsertDto(groupDto).getOrElse { return Err(it) }

        Timber.i("insert: added new Group@%d", groupDto.id)

        return Ok(Unit)
    }

    override suspend fun update(group: GroupModel): DataResult<Unit> {
        val groupDto =
            groupApi.update(group.info.groupId, group.toDto()).getOrElse { return Err(it) }

        upsertDto(groupDto).getOrElse { return Err(it) }

        return Ok(Unit)
    }

    override suspend fun acceptInvite(
        groupId: Long,
        expires: Long,
        nonce: String,
        signature: String,
    ): DataResult<Unit> {
        val dto = groupApi.joinFromInviteUrl(
            groupId = groupId,
            expires = expires,
            nonce = nonce,
            signature = signature,
        ).getOrElse { return Err(it) }

        upsertDto(dto).getOrElse { return Err(it) }

        Timber.i("acceptInvite: accepted invite to groupId=%d", groupId)

        return Ok(Unit)
    }

    private suspend fun refreshUserGroups(userId: Long): DataResult<Unit> = withContext(io) {
        Timber.i("refreshUserGroups: refreshing groups for userId=${userId}")

        val groups = userApi.getGroups(userId).getOrElse { return@withContext Err(it) }

        val results = groups
            .map { group ->
                async {
                    upsertDto(group)
                }
            }
            .awaitAll()

        val err = results.filterErrors().firstOrNull()
        if (err != null) {
            Timber.w("refreshUserGroups: refreshFailed: err=%s", err)
            return@withContext Err(err)
        }

        Timber.i("refreshUserGroups: completed successfully, groupIds=%s", groups.map { it.id })

        Ok(Unit)
    }
}