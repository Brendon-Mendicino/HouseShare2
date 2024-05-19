package lol.terabrendon.houseshare2.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow
import lol.terabrendon.houseshare2.entity.Expense
import lol.terabrendon.houseshare2.entity.ExpenseWithUsers
import lol.terabrendon.houseshare2.entity.Payment

@Dao
interface ExpenseDao {
    @Insert
    suspend fun insertExpense(expense: Expense): Long

    @Insert
    suspend fun insertAllPayment(payments: List<Payment>)

    @Insert
    @Transaction
    suspend fun insertExpense(expense: Expense, payments: List<Payment>) {
        val expenseId = insertExpense(expense)
        insertAllPayment(payments.map { it.copy(expenseId = expenseId) })
    }

    @Query("SELECT * FROM Expense")
    @Transaction
    fun findAll(): Flow<List<ExpenseWithUsers>>

    @Query("SELECT * FROM Expense WHERE id=:expenseId")
    @Transaction
    fun findByExpenseId(expenseId: Int): Flow<ExpenseWithUsers>
}