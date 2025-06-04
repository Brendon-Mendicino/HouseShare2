package lol.terabrendon.houseshare2.data.repository

import kotlinx.coroutines.flow.Flow
import lol.terabrendon.houseshare2.domain.model.GroupModel

interface GroupRepository {
    fun findById(groupId: Long): Flow<GroupModel?>

    suspend fun insert(group: GroupModel)
}