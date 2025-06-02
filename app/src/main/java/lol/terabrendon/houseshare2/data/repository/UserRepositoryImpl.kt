package lol.terabrendon.houseshare2.data.repository

import android.util.Log
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import lol.terabrendon.houseshare2.data.dao.UserDao
import lol.terabrendon.houseshare2.data.entity.User
import lol.terabrendon.houseshare2.domain.model.UserModel
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val userDao: UserDao,
) : UserRepository {
    companion object {
        private const val TAG = "UserRepository"
    }

    override fun findAll(): Flow<List<UserModel>> = userDao.findAll().map { users ->
        users.map { UserModel.from(it) }
    }

    override fun findById(id: Long): Flow<UserModel?> =
        userDao.findById(id).map { it?.let { UserModel.from(it) } }

    override fun findAllById(ids: List<Long>): Flow<List<UserModel>> =
        userDao.findAllById(ids).map { users -> users.map { UserModel.from(it) } }

    override suspend fun insert(user: UserModel) {
        val user = User.from(user)
        val newId = userDao.insert(user)

        Log.i(TAG, "insert: added new user: ${user.copy(id = newId)}")
    }
}