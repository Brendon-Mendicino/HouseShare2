package lol.terabrendon.houseshare2.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import lol.terabrendon.houseshare2.data.entity.ShoppingItem
import lol.terabrendon.houseshare2.data.entity.composite.ShoppingItemWithUser
import java.time.LocalDateTime

@Dao
interface ShoppingItemDao {
    @Query("SELECT * FROM ShoppingItem")
    fun findAll(): Flow<List<ShoppingItem>>

    @Query("SELECT * FROM ShoppingItem WHERE groupId=:groupId")
    @Transaction
    fun findAllByGroupId(groupId: Long): Flow<List<ShoppingItemWithUser>>

    @Query("SELECT * FROM ShoppingItem WHERE id=:id")
    fun findById(id: Long): Flow<ShoppingItem?>

    @Insert
    suspend fun insert(shoppingItem: ShoppingItem): Long

    @Upsert
    suspend fun upsert(shoppingItem: ShoppingItem): Long

    @Query("DELETE FROM ShoppingItem WHERE id in (:itemIds)")
    suspend fun deleteAllById(itemIds: List<Long>)

    @Query("delete from ShoppingItem where id=:shoppingItemId")
    suspend fun deleteById(shoppingItemId: Long)

    @Query("update ShoppingItem set checkingUserId=:checkingUserId, checkoffTimestamp=:checkoffTimestamp where id=:shoppingItemId")
    suspend fun check(shoppingItemId: Long, checkingUserId: Long, checkoffTimestamp: LocalDateTime)

    @Query("update ShoppingItem set checkingUserId=null, checkoffTimestamp=null where id=:shoppingItemId")
    suspend fun uncheck(shoppingItemId: Long)
}