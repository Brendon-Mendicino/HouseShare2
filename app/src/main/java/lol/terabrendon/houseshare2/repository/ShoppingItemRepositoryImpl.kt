package lol.terabrendon.houseshare2.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import lol.terabrendon.houseshare2.dao.ShoppingItemDao
import lol.terabrendon.houseshare2.entity.ShoppingItem
import lol.terabrendon.houseshare2.model.ShoppingItemModel
import javax.inject.Inject

class ShoppingItemRepositoryImpl @Inject constructor(
    private val shoppingItemDao: ShoppingItemDao
) : ShoppingItemRepository {
    override fun getAll(): Flow<List<ShoppingItemModel>> = shoppingItemDao
        .findAll()
        .map { it.map { item -> ShoppingItemModel.from(item) } }

    override suspend fun insert(newItem: ShoppingItemModel) {
        shoppingItemDao.insert(ShoppingItem.from(newItem))
    }
}