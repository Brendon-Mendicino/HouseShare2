package lol.terabrendon.houseshare2.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
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

    @Insert
    suspend fun addUser(ref: GroupUserCrossRef)

    @Insert
    @Transaction
    suspend fun createGroup(group: Group, userIds: List<Long>): Long {
        val newGroupId = addGroup(group)

        userIds
            .map { GroupUserCrossRef(groupId = newGroupId, userId = it) }
            .forEach { addUser(it) }

        return newGroupId
    }

}