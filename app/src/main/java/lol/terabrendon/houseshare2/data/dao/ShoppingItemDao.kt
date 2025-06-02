package lol.terabrendon.houseshare2.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import lol.terabrendon.houseshare2.data.entity.ShoppingItem

@Dao
interface ShoppingItemDao {
    @Query("SELECT * FROM ShoppingItem")
    fun findAll(): Flow<List<ShoppingItem>>

    @Query("SELECT * FROM ShoppingItem WHERE id=:id")
    fun findById(id: Long): Flow<ShoppingItem?>

    @Insert
    suspend fun insert(shoppingItem: ShoppingItem): Long

    @Query("DELETE FROM ShoppingItem WHERE id in (:itemIds)")
    suspend fun deleteAllById(itemIds: List<Long>)
}