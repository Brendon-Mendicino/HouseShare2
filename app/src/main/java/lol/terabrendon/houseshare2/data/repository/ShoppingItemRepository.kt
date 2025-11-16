package lol.terabrendon.houseshare2.data.repository

import kotlinx.coroutines.flow.Flow
import lol.terabrendon.houseshare2.domain.model.ShoppingItemInfoModel
import lol.terabrendon.houseshare2.domain.model.ShoppingItemModel

interface ShoppingItemRepository {
    enum class Sorting {
        CreationDate,
        Priority,
        Name,
        Username,
        ;
    }

    fun getAll(): Flow<List<ShoppingItemInfoModel>>

    fun findAllByGroupId(groupId: Long): Flow<List<ShoppingItemModel>>

    fun findById(shoppingItemId: Long): Flow<ShoppingItemModel?>

    fun findUnchecked(groupId: Long, sorting: Sorting): Flow<List<ShoppingItemModel>>

    fun findChecked(groupId: Long, sorting: Sorting): Flow<List<ShoppingItemModel>>

    suspend fun refreshByGroupId(groupId: Long)

    suspend fun insert(newItem: ShoppingItemInfoModel)

    suspend fun update(item: ShoppingItemInfoModel)

    suspend fun deleteAll(items: List<ShoppingItemInfoModel>)

    suspend fun checkoffItems(groupId: Long, shoppingItemIds: List<Long>, userId: Long)

    suspend fun uncheckItems(groupId: Long, shoppingItemIds: List<Long>)
}