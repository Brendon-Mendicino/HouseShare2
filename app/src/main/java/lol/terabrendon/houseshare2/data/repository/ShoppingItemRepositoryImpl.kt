package lol.terabrendon.houseshare2.data.repository

import android.util.Log
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import lol.terabrendon.houseshare2.data.api.ShoppingApi
import lol.terabrendon.houseshare2.data.dao.CheckoffStateDao
import lol.terabrendon.houseshare2.data.dao.ShoppingItemDao
import lol.terabrendon.houseshare2.data.dto.ShoppingItemDto
import lol.terabrendon.houseshare2.data.entity.CheckoffState
import lol.terabrendon.houseshare2.data.entity.ShoppingItem
import lol.terabrendon.houseshare2.data.entity.composite.ShoppingItemWithUser
import lol.terabrendon.houseshare2.di.IoDispatcher
import lol.terabrendon.houseshare2.domain.mapper.Mapper
import lol.terabrendon.houseshare2.domain.model.ShoppingItemInfoModel
import lol.terabrendon.houseshare2.domain.model.ShoppingItemModel
import lol.terabrendon.houseshare2.domain.model.UserModel
import javax.inject.Inject

class ShoppingItemRepositoryImpl @Inject constructor(
    private val shoppingApi: ShoppingApi,
    private val shoppingItemDao: ShoppingItemDao,
    private val checkoffStateDao: CheckoffStateDao,
    private val modelToDtoMapper: Mapper<ShoppingItemInfoModel, ShoppingItemDto>,
    private val modelToEntityMapper: Mapper<ShoppingItemInfoModel, ShoppingItem>,
    private val dtoMapper: Mapper<ShoppingItemDto, ShoppingItem>,
    private val entityToModelMapper: Mapper<ShoppingItemWithUser, ShoppingItemModel>,
    private val externalScope: CoroutineScope,
    @IoDispatcher
    private val ioDispatcher: CoroutineDispatcher,
) : ShoppingItemRepository {
    companion object {
        private const val TAG = "ShoppingItemRepositoryImpl"
    }

    override fun getAll(): Flow<List<ShoppingItemInfoModel>> = shoppingItemDao
        .findAll()
        .map { it.map { item -> ShoppingItemInfoModel.from(item) } }

    override fun findAllByGroupId(groupId: Long): Flow<List<ShoppingItemModel>> = shoppingItemDao
        .findAllByGroupId(groupId)
        .map { it.map(entityToModelMapper::map) }

    override suspend fun refreshByGroupId(groupId: Long) {
        externalScope.launch(ioDispatcher) {
            Log.i(TAG, "refreshByGroupId: refreshing shopping items of group.id=${groupId}")

            val local = shoppingItemDao.findAllByGroupId(groupId).first()

            val toRemove = local.map { it.item.id }.toMutableSet()

            // Get remote dto
            shoppingApi.getByGroupId(groupId).content
                .map { dtoMapper.map(it) }
                .onEach { toRemove.remove(it.id) }
                .map { launch { shoppingItemDao.upsert(it) } }
                .joinAll()

            toRemove.map { launch { shoppingItemDao.deleteById(it) } }.joinAll()
        }.join()
    }

    override suspend fun insert(newItem: ShoppingItemInfoModel) {
        externalScope.launch(ioDispatcher) {
            Log.i(TAG, "insert: inserting new shopping item to the db.")

            val dto = shoppingApi.save(newItem.groupId, modelToDtoMapper.map(newItem))

            shoppingItemDao.insert(modelToEntityMapper.map(newItem.copy(id = dto.id)))
        }.join()
    }

    override suspend fun deleteAll(items: List<ShoppingItemInfoModel>) {
        externalScope.launch(ioDispatcher) {
            Log.i(TAG, "deleteAll: deleting ${items.size} shopping items from the db.")

            items.map { launch { shoppingApi.delete(it.groupId, it.id) } }.joinAll()

            shoppingItemDao.deleteAllById(items.map { it.id })
        }.join()
    }

    override suspend fun checkoffItems(
        shoppingItemIds: List<Long>,
        user: UserModel,
    ) {
        val checkoffs = shoppingItemIds.map {
            CheckoffState(shoppingItemId = it, checkingUserId = user.id)
        }

        checkoffStateDao.insertCheckoffs(checkoffs)
    }
}