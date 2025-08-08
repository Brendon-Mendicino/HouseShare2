package lol.terabrendon.houseshare2.data.repository

import android.util.Log
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import lol.terabrendon.houseshare2.data.dao.ShoppingItemDao
import lol.terabrendon.houseshare2.data.entity.ShoppingItem
import lol.terabrendon.houseshare2.data.entity.composite.ShoppingItemWithUser
import lol.terabrendon.houseshare2.domain.mapper.Mapper
import lol.terabrendon.houseshare2.domain.model.ShoppingItemInfoModel
import lol.terabrendon.houseshare2.domain.model.ShoppingItemModel
import javax.inject.Inject

class ShoppingItemRepositoryImpl @Inject constructor(
    private val shoppingItemDao: ShoppingItemDao,
    private val shoppingItemModelMapper: Mapper<ShoppingItemWithUser, ShoppingItemModel>,
) : ShoppingItemRepository {
    companion object {
        private const val TAG = "ShoppingItemRepositoryImpl"
    }

    override fun getAll(): Flow<List<ShoppingItemInfoModel>> = shoppingItemDao
        .findAll()
        .map { it.map { item -> ShoppingItemInfoModel.from(item) } }

    override fun findAllByGroupId(groupId: Long): Flow<List<ShoppingItemModel>> = shoppingItemDao
        .findAllByGroupId(groupId)
        .map { it.map(shoppingItemModelMapper::map) }

    override suspend fun insert(newItem: ShoppingItemInfoModel) {
        Log.i(TAG, "insert: inserting new shopping item to the db.")
        shoppingItemDao.insert(ShoppingItem.from(newItem))
    }

    override suspend fun deleteAll(items: List<ShoppingItemInfoModel>) {
        Log.i(TAG, "deleteAll: deleting ${items.size} shopping items from the db.")
        shoppingItemDao.deleteAllById(items.map { it.id })
    }
}