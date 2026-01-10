package lol.terabrendon.houseshare2.data.repository

import android.util.Log
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.coroutineBinding
import com.github.michaelbull.result.onFailure
import com.github.michaelbull.result.onSuccess
import lol.terabrendon.houseshare2.data.local.dao.UserDao
import lol.terabrendon.houseshare2.data.local.util.localSafe
import lol.terabrendon.houseshare2.data.remote.api.UserApi
import lol.terabrendon.houseshare2.domain.error.DataError
import lol.terabrendon.houseshare2.domain.mapper.toEntity
import lol.terabrendon.houseshare2.domain.mapper.toModel
import lol.terabrendon.houseshare2.domain.model.UserModel
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val userDao: UserDao,
    private val userDataRepository: UserDataRepository,
    private val userApi: UserApi,
) : AuthRepository {
    companion object {
        private const val TAG = "AuthRepositoryImpl"
    }

    private suspend fun refreshUser(): Result<UserModel, DataError> = coroutineBinding {
        val user = userApi.getLoggedUser().bind()

        localSafe {
            userDataRepository.updateCurrentLoggedUser(user.id)
            userDao.upsert(user.toEntity())
        }.bind()

        user.toModel()
    }

    override suspend fun finishLogin(): Result<UserModel, DataError> {
        // Reset the current user. Sometimes when the session expires
        // the current user stays logged in, many functions will collect
        // the flow using .distinctUntilChanged(). This prevents
        // those function from not receiving the updated id.
        userDataRepository.updateCurrentLoggedUser(null)

        return refreshUser()
            .onSuccess { user ->
                Log.i(TAG, "finishLogin: got logged user from server. user=$user")
            }
            .onFailure {
                Log.e(TAG, "finishLogin: failed to finish login! error=$it")
            }
    }

    // TODO: for now the loggedUser and finishLogin are identical but may change in the future
    override suspend fun loggedUser(): Result<UserModel, DataError> = refreshUser()
        .onSuccess { user ->
            Log.i(TAG, "loggedUser: got logged user from server. $user")
        }
}