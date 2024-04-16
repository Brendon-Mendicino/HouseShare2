package lol.terabrendon.houseshare2.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import lol.terabrendon.houseshare2.entity.ShoppingItem

@Dao
interface ShoppingItemDao {
    @Query("SELECT * FROM ShoppingItem")
    fun findAll(): Flow<List<ShoppingItem>>

    @Query("SELECT * FROM ShoppingItem WHERE id=:id")
    fun findById(id: Int): Flow<ShoppingItem?>

    @Insert
    suspend fun insert(shoppingItem: ShoppingItem)

    @Query("DELETE FROM ShoppingItem WHERE id in (:itemIds)")
    suspend fun deleteAllById(itemIds: List<Int>)
}