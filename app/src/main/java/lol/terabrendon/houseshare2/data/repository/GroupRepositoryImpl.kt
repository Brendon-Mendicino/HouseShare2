package lol.terabrendon.houseshare2.data.repository

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import lol.terabrendon.houseshare2.data.entity.Group
import lol.terabrendon.houseshare2.data.entity.composite.GroupWithUsers
import lol.terabrendon.houseshare2.data.local.dao.GroupDao
import lol.terabrendon.houseshare2.data.remote.api.GroupApi
import lol.terabrendon.houseshare2.data.remote.dto.GroupDto
import lol.terabrendon.houseshare2.domain.mapper.Mapper
import lol.terabrendon.houseshare2.domain.model.GroupModel
import javax.inject.Inject

class GroupRepositoryImpl @Inject constructor(
    private val groupDao: GroupDao,
    private val groupApi: GroupApi,
    private val entityMapper: Mapper<GroupWithUsers, GroupModel>,
    private val groupModelToDto: Mapper<GroupModel, GroupDto>,
    private val externalScope: CoroutineScope,
) : GroupRepository {
    companion object {
        private const val TAG = "GroupRepository"
    }

    override fun findById(groupId: Long): Flow<GroupModel?> =
        groupDao.findById(groupId).map { group -> group?.let { entityMapper.map(it) } }

    override suspend fun insert(group: GroupModel) = externalScope.launch {
        val groupDto = groupApi.save(groupModelToDto.map(group))

        val groupEntity = Group.from(group)
        val userIds = group.users.map { it.id }

        val newGroupId = groupDao.createGroup(groupEntity.copy(id = groupDto.id), userIds)

        Log.i(TAG, "insert: added new Group@${newGroupId}")
    }.join()
}