package lol.terabrendon.houseshare2.repository

import android.util.Log
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import lol.terabrendon.houseshare2.dao.ShoppingItemDao
import lol.terabrendon.houseshare2.entity.ShoppingItem
import lol.terabrendon.houseshare2.model.ShoppingItemModel
import javax.inject.Inject

class ShoppingItemRepositoryImpl @Inject constructor(
    private val shoppingItemDao: ShoppingItemDao
) : ShoppingItemRepository {
    companion object {
        private const val TAG = "ShoppingItemRepositoryImpl"
    }

    override fun getAll(): Flow<List<ShoppingItemModel>> = shoppingItemDao
        .findAll()
        .map { it.map { item -> ShoppingItemModel.from(item) } }

    override suspend fun insert(newItem: ShoppingItemModel) {
        Log.i(TAG, "insert: inserting new shopping item to the db.")
        shoppingItemDao.insert(ShoppingItem.from(newItem))
    }

    override suspend fun deleteAll(items: List<ShoppingItemModel>) {
        Log.i(TAG, "deleteAll: deleting ${items.size} shopping items from the db.")
        shoppingItemDao.deleteAllById(items.map { it.id })
    }
}