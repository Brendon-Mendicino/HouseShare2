package lol.terabrendon.houseshare2.data.repository

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import lol.terabrendon.houseshare2.data.local.dao.ShoppingItemDao
import lol.terabrendon.houseshare2.data.remote.api.ShoppingApi
import lol.terabrendon.houseshare2.data.remote.dto.CheckDto
import lol.terabrendon.houseshare2.di.IoDispatcher
import lol.terabrendon.houseshare2.domain.mapper.toDto
import lol.terabrendon.houseshare2.domain.mapper.toEntity
import lol.terabrendon.houseshare2.domain.mapper.toModel
import lol.terabrendon.houseshare2.domain.model.ShoppingItemInfoModel
import lol.terabrendon.houseshare2.domain.model.ShoppingItemModel
import lol.terabrendon.houseshare2.util.mapInner
import timber.log.Timber
import java.time.OffsetDateTime
import javax.inject.Inject

class ShoppingItemRepositoryImpl @Inject constructor(
    private val shoppingApi: ShoppingApi,
    private val shoppingItemDao: ShoppingItemDao,
    private val externalScope: CoroutineScope,
    @param:IoDispatcher
    private val ioDispatcher: CoroutineDispatcher,
) : ShoppingItemRepository {
    override fun getAll(): Flow<List<ShoppingItemInfoModel>> = shoppingItemDao
        .findAll()
        .map { it.map { item -> item.toModel() } }

    override fun findAllByGroupId(groupId: Long): Flow<List<ShoppingItemModel>> = shoppingItemDao
        .findAllByGroupId(groupId)
        .map { it.map { item -> item.toModel() } }

    override fun findById(shoppingItemId: Long): Flow<ShoppingItemModel?> =
        shoppingItemDao.findById(shoppingItemId).distinctUntilChanged().map { it?.toModel() }

    override fun findUnchecked(
        groupId: Long,
        sorting: ShoppingItemRepository.Sorting,
    ): Flow<List<ShoppingItemModel>> = shoppingItemDao.findUnchecked(groupId, sorting)
        .mapInner { it.toModel() }

    override fun findChecked(
        groupId: Long,
        sorting: ShoppingItemRepository.Sorting,
    ): Flow<List<ShoppingItemModel>> = shoppingItemDao.findChecked(groupId, sorting)
        .mapInner { it.toModel() }

    override suspend fun refreshByGroupId(groupId: Long) {
        externalScope.launch(ioDispatcher) {
            Timber.i("refreshByGroupId: refreshing shopping items of group.id=%d", groupId)

            val local = shoppingItemDao.findAllByGroupId(groupId).first()

            val toRemove = local.map { it.item.id }.toMutableSet()

            // Get remote dto
            shoppingApi.getByGroupId(groupId).content
                .map { it.toEntity() }
                .onEach { toRemove.remove(it.id) }
                .map { launch { shoppingItemDao.upsert(it) } }
                .joinAll()

            shoppingItemDao.deleteAllById(toRemove.toList())
        }.join()
    }


    override suspend fun insert(newItem: ShoppingItemInfoModel) {
        externalScope.launch(ioDispatcher) {
            Timber.i("insert: inserting new shopping item to the db.")

            val dto = shoppingApi.save(newItem.groupId, newItem.toDto())

            shoppingItemDao.insert(newItem.copy(id = dto.id).toEntity())
        }.join()
    }

    override suspend fun update(item: ShoppingItemInfoModel) {
        externalScope.launch(ioDispatcher) {
            Timber.i("update: updating shopping item")

            val dto = shoppingApi.update(item.groupId, item.id, item.toDto())

            shoppingItemDao.upsert(dto.toEntity())
        }.join()
    }

    override suspend fun deleteAll(items: List<ShoppingItemInfoModel>) {
        externalScope.launch(ioDispatcher) {
            Timber.i("deleteAll: deleting %d shopping items from the db.", items.size)

            items.map { launch { shoppingApi.delete(it.groupId, it.id) } }.joinAll()

            shoppingItemDao.deleteAllById(items.map { it.id })
        }.join()
    }

    override suspend fun checkoffItems(
        groupId: Long,
        shoppingItemIds: List<Long>,
        userId: Long,
    ) {
        externalScope.launch(ioDispatcher) {
            Timber.i("checkoffItems: userId=%d itemIds=%s", userId, shoppingItemIds)
            val timestamp = OffsetDateTime.now()

            shoppingItemIds
                .map {
                    launch {
                        shoppingApi.checkShoppingItem(
                            groupId = groupId,
                            shoppingItemId = it,
                            dto = CheckDto(checkingUserId = userId, checkoffTimestamp = timestamp),
                        )
                        shoppingItemDao.check(it, userId, timestamp.toLocalDateTime())
                    }
                }
                .joinAll()
        }.join()
    }

    override suspend fun uncheckItems(
        groupId: Long,
        shoppingItemIds: List<Long>,
    ) {
        externalScope.launch(ioDispatcher) {
            Timber.i("uncheckItems: itemIds=%s", shoppingItemIds)

            shoppingItemIds.map { shoppingId ->
                launch {
                    shoppingApi.uncheckShoppingItem(groupId = groupId, shoppingItemId = shoppingId)
                    shoppingItemDao.uncheck(shoppingId)
                }
            }.joinAll()
        }
    }
}