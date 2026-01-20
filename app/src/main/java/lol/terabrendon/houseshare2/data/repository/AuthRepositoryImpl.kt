package lol.terabrendon.houseshare2.data.repository

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.getOrElse
import com.github.michaelbull.result.onFailure
import com.github.michaelbull.result.onSuccess
import lol.terabrendon.houseshare2.data.local.dao.UserDao
import lol.terabrendon.houseshare2.data.local.util.localSafe
import lol.terabrendon.houseshare2.data.remote.api.UserApi
import lol.terabrendon.houseshare2.data.repository.UserDataRepository.Update.LoggedUserId
import lol.terabrendon.houseshare2.domain.error.DataError
import lol.terabrendon.houseshare2.domain.mapper.toEntity
import lol.terabrendon.houseshare2.domain.mapper.toModel
import lol.terabrendon.houseshare2.domain.model.UserModel
import timber.log.Timber
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val userDao: UserDao,
    private val userDataRepository: UserDataRepository,
    private val userApi: UserApi,
) : AuthRepository {
    private suspend fun refreshUser(): Result<UserModel, DataError> {
        val user = userApi.getLoggedUser().getOrElse { return Err(it) }

        localSafe {
            userDataRepository.update(LoggedUserId(user.id))
            userDao.upsert(user.toEntity())
        }.getOrElse { return Err(it) }

        return Ok(user.toModel())
    }

    override suspend fun finishLogin(): Result<UserModel, DataError> {
        // Reset the current user. Sometimes when the session expires
        // the current user stays logged in, many functions will collect
        // the flow using .distinctUntilChanged(). This prevents
        // those function from not receiving the updated id.
        userDataRepository.update(LoggedUserId(null))

        return refreshUser()
            .onSuccess { user ->
                Timber.i("finishLogin: got logged user from server. user=%s", user)
            }
            .onFailure {
                Timber.e("finishLogin: failed to finish login! error=%s", it)
            }
    }

    override suspend fun loggedUser(): Result<UserModel, DataError> = refreshUser()
        .onSuccess { user ->
            Timber.i("loggedUser: got logged user from server. user=%s", user)
        }
        .onFailure { err ->
            Timber.w("loggedUser: returned an error: err=%s", err)
        }
}