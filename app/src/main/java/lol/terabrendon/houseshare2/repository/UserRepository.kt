package lol.terabrendon.houseshare2.repository

import kotlinx.coroutines.flow.Flow
import lol.terabrendon.houseshare2.model.UserModel

interface UserRepository {

    fun findAll(): Flow<List<UserModel>>

    fun findById(id: Int): Flow<UserModel?>

    fun findAllById(ids: List<Int>): Flow<List<UserModel>>

    suspend fun insert(user: UserModel)
}