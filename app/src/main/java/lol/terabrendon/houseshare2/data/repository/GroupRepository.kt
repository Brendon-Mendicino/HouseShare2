package lol.terabrendon.houseshare2.data.repository

import kotlinx.coroutines.flow.Flow
import lol.terabrendon.houseshare2.data.util.DataResult
import lol.terabrendon.houseshare2.domain.model.GroupInfoModel
import lol.terabrendon.houseshare2.domain.model.GroupMemberModel
import lol.terabrendon.houseshare2.domain.model.GroupModel
import lol.terabrendon.houseshare2.domain.model.UserModel

interface GroupRepository {
    fun findById(groupId: Long): Flow<GroupModel?>

    fun findGroupsByUserId(userId: Long): Flow<List<GroupInfoModel>>

    suspend fun findOrFetchUser(groupId: Long, userId: Long): DataResult<UserModel?>

    suspend fun findOrFetchMember(groupId: Long, memberId: Long): DataResult<GroupMemberModel?>

    suspend fun addMember(member: GroupMemberModel): DataResult<GroupMemberModel>

    suspend fun updateMember(member: GroupMemberModel): DataResult<GroupMemberModel>

    suspend fun insert(group: GroupModel): DataResult<Unit>

    suspend fun update(group: GroupModel): DataResult<Unit>

    suspend fun acceptInvite(
        groupId: Long,
        expires: Long,
        nonce: String,
        signature: String,
    ): DataResult<Unit>
}