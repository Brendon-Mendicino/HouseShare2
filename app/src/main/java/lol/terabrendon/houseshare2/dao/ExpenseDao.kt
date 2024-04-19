package lol.terabrendon.houseshare2.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow
import lol.terabrendon.houseshare2.entity.Expense
import lol.terabrendon.houseshare2.entity.ExpenseOfUser
import lol.terabrendon.houseshare2.entity.ExpenseWithUsers

@Dao
interface ExpenseDao {
    @Insert
    suspend fun insertExpense(expense: Expense)

    @Insert
    suspend fun insertAllExpenseOfUser(expenseOfUsers: List<ExpenseOfUser>)

    @Insert
    @Transaction
    suspend fun insertExpense(expense: ExpenseWithUsers) {
        insertExpense(expense.expense)
        insertAllExpenseOfUser(expense.users)
    }

    @Query("SELECT * FROM Expense")
    @Transaction
    fun findAll(): Flow<List<ExpenseWithUsers>>

    @Query("SELECT * FROM Expense WHERE id=:expenseId")
    @Transaction
    fun findByExpenseId(expenseId: Int): Flow<ExpenseWithUsers>
}