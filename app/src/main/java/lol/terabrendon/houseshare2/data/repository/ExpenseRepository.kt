package lol.terabrendon.houseshare2.data.repository

import kotlinx.coroutines.flow.Flow
import lol.terabrendon.houseshare2.domain.model.ExpenseModel

interface ExpenseRepository {

    fun findAll(): Flow<List<ExpenseModel>>

    fun findByGroupId(groupId: Long): Flow<List<ExpenseModel>>

    suspend fun insert(expense: ExpenseModel)
}