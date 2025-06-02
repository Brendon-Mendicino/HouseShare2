package lol.terabrendon.houseshare2.data.repository

import kotlinx.coroutines.flow.Flow
import lol.terabrendon.houseshare2.domain.model.ShoppingItemModel

interface ShoppingItemRepository {
    fun getAll(): Flow<List<ShoppingItemModel>>

    suspend fun insert(newItem: ShoppingItemModel)

    suspend fun deleteAll(items: List<ShoppingItemModel>)
}