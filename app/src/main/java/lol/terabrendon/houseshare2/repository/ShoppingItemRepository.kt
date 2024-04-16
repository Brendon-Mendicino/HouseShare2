package lol.terabrendon.houseshare2.repository

import kotlinx.coroutines.flow.Flow
import lol.terabrendon.houseshare2.model.ShoppingItemModel

interface ShoppingItemRepository {
    fun getAll(): Flow<List<ShoppingItemModel>>

    suspend fun insert(newItem: ShoppingItemModel)

    suspend fun deleteAll(items: List<ShoppingItemModel>)
}