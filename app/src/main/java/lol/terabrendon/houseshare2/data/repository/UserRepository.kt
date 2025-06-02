package lol.terabrendon.houseshare2.data.repository

import kotlinx.coroutines.flow.Flow
import lol.terabrendon.houseshare2.domain.model.UserModel

interface UserRepository {

    fun findAll(): Flow<List<UserModel>>

    fun findById(id: Long): Flow<UserModel?>

    fun findAllById(ids: List<Long>): Flow<List<UserModel>>

    suspend fun insert(user: UserModel)
}