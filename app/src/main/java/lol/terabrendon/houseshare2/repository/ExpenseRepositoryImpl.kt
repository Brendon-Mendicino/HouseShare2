package lol.terabrendon.houseshare2.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import lol.terabrendon.houseshare2.dao.ExpenseDao
import lol.terabrendon.houseshare2.entity.ExpenseWithUsers
import lol.terabrendon.houseshare2.model.ExpenseModel
import javax.inject.Inject

class ExpenseRepositoryImpl @Inject constructor(
    private val expenseDao: ExpenseDao
) : ExpenseRepository {
    override fun findAll(): Flow<List<ExpenseModel>> =
        expenseDao.findAll().map { expenses ->
            expenses.map { ExpenseModel.from(it) }
        }

    override suspend fun insert(expense: ExpenseModel) {
        expenseDao.insertExpense(ExpenseWithUsers.from(expense))
    }
}