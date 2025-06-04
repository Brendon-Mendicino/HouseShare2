package lol.terabrendon.houseshare2.data.repository

import android.util.Log
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import lol.terabrendon.houseshare2.data.dao.GroupDao
import lol.terabrendon.houseshare2.data.entity.Group
import lol.terabrendon.houseshare2.domain.mapper.GroupEntityMapper
import lol.terabrendon.houseshare2.domain.model.GroupModel
import javax.inject.Inject

class GroupRepositoryImpl @Inject constructor(
    private val groupDao: GroupDao,
) : GroupRepository {
    companion object {
        private const val TAG = "GroupRepository"
    }

    private val groupMapper = GroupEntityMapper()

    override fun findById(groupId: Long): Flow<GroupModel?> =
        groupDao.findById(groupId).map { group -> group?.let { groupMapper.map(it) } }

    override suspend fun insert(group: GroupModel) {
        val groupEntity = Group.from(group)
        val userIds = group.users.map { it.id }

        val newGroupId = groupDao.createGroup(groupEntity, userIds)

        Log.i(TAG, "insert: added new Group@${newGroupId}")
    }
}