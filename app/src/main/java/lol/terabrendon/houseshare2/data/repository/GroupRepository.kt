package lol.terabrendon.houseshare2.data.repository

import kotlinx.coroutines.flow.Flow
import lol.terabrendon.houseshare2.data.util.DataResult
import lol.terabrendon.houseshare2.domain.model.GroupModel

interface GroupRepository {
    fun findById(groupId: Long): Flow<GroupModel?>

    suspend fun insert(group: GroupModel): DataResult<Unit>

    suspend fun acceptInvite(
        groupId: Long,
        expires: Long,
        nonce: String,
        signature: String,
    ): DataResult<Unit>
}