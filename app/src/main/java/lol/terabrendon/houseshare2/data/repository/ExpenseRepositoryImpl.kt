package lol.terabrendon.houseshare2.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import lol.terabrendon.houseshare2.data.entity.Expense
import lol.terabrendon.houseshare2.data.entity.Payment
import lol.terabrendon.houseshare2.data.local.dao.ExpenseDao
import lol.terabrendon.houseshare2.domain.model.ExpenseModel
import javax.inject.Inject

class ExpenseRepositoryImpl @Inject constructor(
    private val expenseDao: ExpenseDao,
) : ExpenseRepository {

    override fun findAll(): Flow<List<ExpenseModel>> = expenseDao.findAll().map { expenses ->
        expenses.map { ExpenseModel.from(it) }
    }

    override fun findByGroupId(groupId: Long): Flow<List<ExpenseModel>> = expenseDao
        .findByGroupId(groupId)
        .map { expenses -> expenses.map { ExpenseModel.from(it) } }

    override suspend fun insert(expense: ExpenseModel) {
        expenseDao.insertExpense(
            expense = Expense.from(expense),
            payments = expense.userExpenses.map {
                Payment(
                    expenseId = expense.id,
                    userId = it.user.id,
                    partAmount = it.partAmount,
                )
            }
        )
    }
}