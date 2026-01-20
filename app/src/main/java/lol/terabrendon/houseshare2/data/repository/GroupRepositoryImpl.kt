package lol.terabrendon.houseshare2.data.repository

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.getOrElse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import lol.terabrendon.houseshare2.data.entity.Group
import lol.terabrendon.houseshare2.data.local.dao.GroupDao
import lol.terabrendon.houseshare2.data.local.util.localSafe
import lol.terabrendon.houseshare2.data.remote.api.GroupApi
import lol.terabrendon.houseshare2.data.util.DataResult
import lol.terabrendon.houseshare2.domain.mapper.toDto
import lol.terabrendon.houseshare2.domain.mapper.toEntity
import lol.terabrendon.houseshare2.domain.mapper.toModel
import lol.terabrendon.houseshare2.domain.model.GroupModel
import timber.log.Timber
import javax.inject.Inject

class GroupRepositoryImpl @Inject constructor(
    private val groupDao: GroupDao,
    private val groupApi: GroupApi,
) : GroupRepository {
    override fun findById(groupId: Long): Flow<GroupModel?> =
        groupDao.findById(groupId).distinctUntilChanged().map { group -> group?.toModel() }

    override suspend fun insert(group: GroupModel): DataResult<Unit> {
        val groupDto = groupApi.save(group.toDto()).getOrElse { return Err(it) }

        val groupEntity = Group.from(group)
        val userIds = group.users.map { it.id }

        val newGroupId =
            localSafe { groupDao.createGroup(groupEntity.copy(id = groupDto.id), userIds) }
                .getOrElse { return Err(it) }

        Timber.i("insert: added new Group@%d", newGroupId)

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

        localSafe { groupDao.addGroup(dto.toEntity()) }.getOrElse { return Err(it) }

        Timber.i("acceptInvite: accepted invite to groupId=%d", groupId)

        return Ok(Unit)
    }
}