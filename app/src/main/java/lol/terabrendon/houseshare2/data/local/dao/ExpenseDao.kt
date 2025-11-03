package lol.terabrendon.houseshare2.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow
import lol.terabrendon.houseshare2.data.entity.Expense
import lol.terabrendon.houseshare2.data.entity.ExpensePart
import lol.terabrendon.houseshare2.data.entity.composite.ExpenseWithUsers

@Dao
interface ExpenseDao {
    @Insert
    suspend fun insertExpense(expense: Expense): Long

    @Insert
    suspend fun insertAllPayment(expenseParts: List<ExpensePart>)

    @Insert
    @Transaction
    suspend fun insertExpense(expense: Expense, expenseParts: List<ExpensePart>) {
        val expenseId = insertExpense(expense)
        insertAllPayment(expenseParts.map { it.copy(expenseId = expenseId) })
    }

    @Query("SELECT * FROM Expense")
    @Transaction
    fun findAll(): Flow<List<ExpenseWithUsers>>

    @Transaction
    @Query("SELECT * FROM Expense WHERE groupId=:groupId")
    fun findByGroupId(groupId: Long): Flow<List<ExpenseWithUsers>>

    @Query("SELECT * FROM Expense WHERE id=:expenseId")
    @Transaction
    fun findByExpenseId(expenseId: Int): Flow<ExpenseWithUsers>
}