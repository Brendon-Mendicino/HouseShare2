package lol.terabrendon.houseshare2.repository

import kotlinx.coroutines.flow.Flow
import lol.terabrendon.houseshare2.model.ExpenseModel

interface ExpenseRepository {

    fun findAll(): Flow<List<ExpenseModel>>

    suspend fun insert(expense: ExpenseModel)
}