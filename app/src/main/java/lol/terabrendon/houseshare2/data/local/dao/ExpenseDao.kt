package lol.terabrendon.houseshare2.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import lol.terabrendon.houseshare2.data.entity.Expense
import lol.terabrendon.houseshare2.data.entity.ExpensePart
import lol.terabrendon.houseshare2.data.entity.composite.ExpenseWithUsers

@Dao
interface ExpenseDao {
    @Insert
    suspend fun insertExpense(expense: Expense): Long

    @Upsert
    suspend fun upsertExpense(expense: Expense): Long

    /**
     * [ExpensePart] as Cascade on the [ExpensePart.expenseId]
     * foreign key. Also those records will be remove.
     */
    @Query("delete from Expense where id=:expenseId")
    suspend fun deleteById(expenseId: Long)

    @Query("delete from Expense where id in (:expenseIds)")
    @Transaction
    suspend fun deleteAllById(expenseIds: List<Long>)

    @Insert
    suspend fun insertAllPayment(expenseParts: List<ExpensePart>)

    @Upsert
    suspend fun upsertAllParts(expenseParts: List<ExpensePart>): LongArray

    @Insert
    @Transaction
    suspend fun insertExpense(expense: Expense, expenseParts: List<ExpensePart>): Long {
        val expenseId = insertExpense(expense)
        insertAllPayment(expenseParts.map { it.copy(expenseId = expenseId) })
        return expenseId
    }


    @Query("delete from ExpensePart where id not in (:partIds) and expenseId=:expenseId")
    suspend fun deletePartsNotIn(partIds: List<Long>, expenseId: Long)

    @Upsert
    @Transaction
    suspend fun upsertExpense(expense: Expense, expenseParts: List<ExpensePart>) {
        val upsertId = upsertExpense(expense)

        val expensePartIds = upsertAllParts(expenseParts)
            .zip(expenseParts)
            .map { (id, part) -> if (id == -1L) part.id else id }

        // If the upsert returned -1 it means that the expense was updated
        if (upsertId == -1L) {
            deletePartsNotIn(expensePartIds, expense.id)
        }
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