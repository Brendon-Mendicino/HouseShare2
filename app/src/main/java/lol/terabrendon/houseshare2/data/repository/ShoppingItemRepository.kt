package lol.terabrendon.houseshare2.data.repository

import kotlinx.coroutines.flow.Flow
import lol.terabrendon.houseshare2.domain.model.ShoppingItemInfoModel
import lol.terabrendon.houseshare2.domain.model.ShoppingItemModel
import lol.terabrendon.houseshare2.domain.model.UserModel

interface ShoppingItemRepository {
    fun getAll(): Flow<List<ShoppingItemInfoModel>>

    fun findAllByGroupId(groupId: Long): Flow<List<ShoppingItemModel>>

    suspend fun insert(newItem: ShoppingItemInfoModel)

    suspend fun deleteAll(items: List<ShoppingItemInfoModel>)

    suspend fun checkoffItems(shoppingItemIds: List<Long>, user: UserModel)
}