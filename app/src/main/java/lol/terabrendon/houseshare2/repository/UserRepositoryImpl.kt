package lol.terabrendon.houseshare2.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import lol.terabrendon.houseshare2.dao.UserDao
import lol.terabrendon.houseshare2.entity.User
import lol.terabrendon.houseshare2.model.UserModel
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val userDao: UserDao,
) : UserRepository {
    override fun findAll(): Flow<List<UserModel>> = userDao.findAll().map { users ->
        users.map { UserModel.from(it) }
    }

    override fun findById(id: Long): Flow<UserModel?> =
        userDao.findById(id).map { it?.let { UserModel.from(it) } }

    override fun findAllById(ids: List<Long>): Flow<List<UserModel>> =
        userDao.findAllById(ids).map { users -> users.map { UserModel.from(it) } }

    override suspend fun insert(user: UserModel) {
        userDao.insert(User.from(user))
    }
}