package lol.terabrendon.houseshare2.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import lol.terabrendon.houseshare2.data.entity.ShoppingItem
import lol.terabrendon.houseshare2.data.entity.composite.ShoppingItemWithUser
import lol.terabrendon.houseshare2.data.repository.ShoppingItemRepository
import java.time.LocalDateTime

@Dao
interface ShoppingItemDao {
    @Query("SELECT * FROM ShoppingItem")
    fun findAll(): Flow<List<ShoppingItem>>

    @Query("SELECT * FROM ShoppingItem WHERE groupId=:groupId")
    @Transaction
    fun findAllByGroupId(groupId: Long): Flow<List<ShoppingItemWithUser>>

    @Query(
        "select s.* from ShoppingItem as s " +
                "join (select id, (case priority when 'Now' then 3 when 'Soon' then 2 when 'Later' then 1 end) as pid from ShoppingItem) as prio on s.id=prio.id " +
                "join `User` as u on u.id=s.ownerId " +
                "where groupId=:groupId and checkingUserId is null " +
                "order by " +
                "case :sorting " +
                "   when 'CreationDate' then creationTimestamp " +
                "   when 'Priority' then prio.pid " +
                "   when 'Name' then lower(name) " +
                "   when 'Username' then lower(u.username) " +
                "end " +
                "desc "
    )
    @Transaction
    fun findUnchecked(
        groupId: Long,
        sorting: ShoppingItemRepository.Sorting = ShoppingItemRepository.Sorting.CreationDate,
    ): Flow<List<ShoppingItemWithUser>>

    @Query(
        "select s.* from ShoppingItem as s " +
                "join (select id, (case priority when 'Now' then 3 when 'Soon' then 2 when 'Later' then 1 end) as pid from ShoppingItem) as prio on s.id=prio.id " +
                "join `User` as u on u.id=s.ownerId " +
                "where groupId=:groupId and not checkingUserId is null " +
                "order by " +
                "case :sorting " +
                "   when 'CreationDate' then creationTimestamp " +
                "   when 'Priority' then prio.pid " +
                "   when 'Name' then lower(name) " +
                "   when 'Username' then lower(u.username) " +
                "end " +
                "desc "
    )
    @Transaction
    fun findChecked(
        groupId: Long,
        sorting: ShoppingItemRepository.Sorting = ShoppingItemRepository.Sorting.CreationDate,
    ): Flow<List<ShoppingItemWithUser>>

    @Query("SELECT * FROM ShoppingItem WHERE id=:id")
    @Transaction
    fun findById(id: Long): Flow<ShoppingItemWithUser?>

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