package lol.terabrendon.houseshare2.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import lol.terabrendon.houseshare2.data.entity.Group
import lol.terabrendon.houseshare2.data.entity.GroupUserCrossRef
import lol.terabrendon.houseshare2.data.entity.composite.GroupWithUsers

@Dao
interface GroupDao {
    @Query("select * from `Group` where id=:groupId")
    @Transaction
    fun findById(groupId: Long): Flow<GroupWithUsers?>

    @Insert
    suspend fun addGroup(group: Group): Long

    @Upsert
    suspend fun upsert(group: Group): Long

    @Query("delete from `Group` where id=:groupId")
    suspend fun delete(groupId: Long)

    @Insert
    suspend fun addUser(ref: GroupUserCrossRef)

    @Upsert
    suspend fun upsertUser(ref: GroupUserCrossRef)

    @Query("delete from GroupUserCrossRef where groupId=:groupId")
    suspend fun deleteGroupUsers(groupId: Long)

    @Insert
    @Transaction
    suspend fun createGroup(group: Group, userIds: List<Long>): Long {
        val newGroupId = addGroup(group)

        userIds
            .map { GroupUserCrossRef(groupId = newGroupId, userId = it) }
            .forEach { addUser(it) }

        return newGroupId
    }

    @Upsert
    @Transaction
    suspend fun upsertGroup(group: Group, userIds: List<Long>): Long {
        val groupId = upsert(group)

        deleteGroupUsers(group.id)

        userIds
            .map { GroupUserCrossRef(groupId = group.id, userId = it) }
            .forEach { upsertUser(it) }

        return groupId
    }

}