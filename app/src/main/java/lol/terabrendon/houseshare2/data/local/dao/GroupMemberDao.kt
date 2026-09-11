package lol.terabrendon.houseshare2.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Upsert
import lol.terabrendon.houseshare2.data.entity.GroupMember

@Dao
interface GroupMemberDao {
    @Insert
    suspend fun insert(member: GroupMember): Long

    @Upsert
    suspend fun upsert(member: GroupMember): Long

    @Upsert
    suspend fun upsertAll(member: List<GroupMember>): LongArray

    @Query("select * from GroupMember where id=:memberId and groupId=:groupId")
    suspend fun findByIdAndGroupId(memberId: Long, groupId: Long): GroupMember?
}