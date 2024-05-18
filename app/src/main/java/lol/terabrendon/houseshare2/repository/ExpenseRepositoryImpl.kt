package lol.terabrendon.houseshare2.repository

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.map
import lol.terabrendon.houseshare2.dao.ExpenseDao
import lol.terabrendon.houseshare2.entity.ExpenseWithUsers
import lol.terabrendon.houseshare2.model.ExpenseModel
import lol.terabrendon.houseshare2.model.UserExpenseModel
import lol.terabrendon.houseshare2.model.UserModel
import javax.inject.Inject

class ExpenseRepositoryImpl @Inject constructor(
    private val expenseDao: ExpenseDao,
    private val userRepository: UserRepository,
) : ExpenseRepository {

    private fun createExpenseModel(
        expenseWithUsers: ExpenseWithUsers,
        users: Map<Int, UserModel>,
    ): ExpenseModel? {
        return ExpenseModel(
            id = expenseWithUsers.expense.id,
            expenseOwner = users[expenseWithUsers.expense.ownerId] ?: return null,
            amount = expenseWithUsers.expense.amount,
            category = expenseWithUsers.expense.category,
            title = expenseWithUsers.expense.title,
            description = expenseWithUsers.expense.description,
            creationTimestamp = expenseWithUsers.expense.creationTimestamp,
            userExpenses = expenseWithUsers.expensesWithUser.map {
                val userModel = users[it.userId] ?: return null

                UserExpenseModel(user = userModel, amount = it.amount)
            }
        )
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun findAll(): Flow<List<ExpenseModel>> {
        // Get users from the database associated to the current expenses
        val usersFlow = expenseDao
            .findAll()
            .flatMapMerge { expenses ->
                val userIds =
                    expenses.flatMap { expense -> expense.expensesWithUser.map { e -> e.userId } }
                        .toMutableSet()

                userIds.addAll(expenses.map { it.expense.ownerId })

                userRepository.findAllById(userIds.toList()).map { users ->
                    users.associateBy { it.id }
                }
            }

        // TODO: create a single fetch from the database
        return expenseDao
            .findAll()
            .combine(usersFlow) { expenses, users ->
                expenses.map { expense ->
                    createExpenseModel(
                        expense,
                        users
                    ) ?: throw IllegalArgumentException(
                        "It was not possible to create a new ${ExpenseModel::class.qualifiedName}! Expense: $expense, Users: $users"
                    )
                }
            }
    }

    override suspend fun insert(expense: ExpenseModel) {
        expenseDao.insertExpense(ExpenseWithUsers.from(expense))
    }
}