package lol.terabrendon.houseshare2.data.repository

import android.util.Log
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.runSuspendCatching
import com.github.michaelbull.result.onFailure
import com.github.michaelbull.result.onSuccess
import lol.terabrendon.houseshare2.data.local.dao.UserDao
import lol.terabrendon.houseshare2.data.remote.api.UserApi
import lol.terabrendon.houseshare2.domain.mapper.toEntity
import lol.terabrendon.houseshare2.domain.mapper.toModel
import lol.terabrendon.houseshare2.domain.model.UserModel
import retrofit2.HttpException
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val userDao: UserDao,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val userApi: UserApi,
) : AuthRepository {
    companion object {
        private const val TAG = "AuthRepositoryImpl"
    }

    private suspend fun refreshUser(): Result<UserModel, Throwable> = runSuspendCatching {
        val response = userApi.getLoggedUser()

        if (!response.isSuccessful) throw HttpException(response)
        val user = response.body() ?: throw HttpException(response)

        userDao.upsert(user.toEntity())
        userPreferencesRepository.updateCurrentLoggedUser(user.id)

        user.toModel()
    }.onFailure {
        userPreferencesRepository.updateCurrentLoggedUser(null)
    }

    override suspend fun finishLogin(): Result<UserModel, Throwable> = refreshUser()
        .onSuccess { user ->
            Log.i(TAG, "finishLogin: got logged user from server. $user")
        }

    // TODO: for now the loggedUser and finishLogin are identical but may change in the future
    override suspend fun loggedUser(): Result<UserModel, Throwable> = refreshUser()
        .onSuccess { user ->
            Log.i(TAG, "loggedUser: got logged user from server. $user")
        }
}