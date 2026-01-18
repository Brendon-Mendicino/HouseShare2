package lol.terabrendon.houseshare2.data.repository

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import lol.terabrendon.houseshare2.data.local.dao.ExpenseDao
import lol.terabrendon.houseshare2.data.local.dao.UserDao
import lol.terabrendon.houseshare2.data.remote.api.ExpenseApi
import lol.terabrendon.houseshare2.di.IoDispatcher
import lol.terabrendon.houseshare2.domain.mapper.toDto
import lol.terabrendon.houseshare2.domain.mapper.toEntity
import lol.terabrendon.houseshare2.domain.mapper.toModel
import lol.terabrendon.houseshare2.domain.model.ExpenseModel
import timber.log.Timber
import javax.inject.Inject

class ExpenseRepositoryImpl @Inject constructor(
    private val expenseDao: ExpenseDao,
    private val expenseApi: ExpenseApi,
    private val externalScope: CoroutineScope,
    @param:IoDispatcher
    private val ioDispatcher: CoroutineDispatcher,
    private val userRepository: UserRepository,
    private val userDao: UserDao,
) : ExpenseRepository {
    override fun findAll(): Flow<List<ExpenseModel>> = expenseDao.findAll().map { expenses ->
        expenses.map { it.toModel() }
    }

    override fun findByGroupId(groupId: Long): Flow<List<ExpenseModel>> = expenseDao
        .findByGroupId(groupId)
        .map { expenses -> expenses.map { it.toModel() } }

    override suspend fun insert(expense: ExpenseModel) = externalScope.launch(ioDispatcher) {
        Timber.i("insert: starting expense insertion")

        val dto = expenseApi.save(expense.groupId, expense.toDto())

        expenseDao.insertExpense(
            expense = dto.toEntity(),
            expenseParts = dto.expenseParts.map { it.toEntity() },
        )
    }.join()

    override suspend fun refreshByGroupId(groupId: Long) = externalScope.launch(ioDispatcher) {
        Timber.i("refreshByGroupId: refreshing expenses of group.id=%d", groupId)

        val local = expenseDao.findByGroupId(groupId).first()

        val toRemove = local.map { it.expense.id }.toMutableSet()

        // Get remote dto
        val dto = expenseApi.getExpenses(groupId).content

        // Refresh expenses users not already present in the db
        dto
            .flatMap { expense -> expense.expenseParts.map { it.userId } }
            .distinct()
            .map { userId ->
                launch {
                    if (!userDao.existById(userId)) {
                        userRepository.refreshGroupUser(groupId, userId)
                    }
                }
            }
            .joinAll()

        // Upsert all the expenses
        dto
            .map { expense -> expense.toEntity() to expense.expenseParts.map { it.toEntity() } }
            .onEach { (expense, _) -> toRemove.remove(expense.id) }
            .map { (expense, parts) -> launch { expenseDao.upsertExpense(expense, parts) } }
            .joinAll()

        expenseDao.deleteAllById(toRemove.toList())
    }.join()
}