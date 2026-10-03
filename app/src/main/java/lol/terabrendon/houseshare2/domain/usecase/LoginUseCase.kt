package lol.terabrendon.houseshare2.domain.usecase

import androidx.room.RoomDatabase
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.getOrElse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import lol.terabrendon.houseshare2.data.repository.AuthRepository
import lol.terabrendon.houseshare2.data.repository.SessionManager
import lol.terabrendon.houseshare2.data.repository.UserDataRepository
import lol.terabrendon.houseshare2.domain.auth.Credentials
import lol.terabrendon.houseshare2.domain.error.DataError
import lol.terabrendon.houseshare2.domain.model.UserModel
import timber.log.Timber
import javax.inject.Inject

/**
 * Logs the user in: runs the whole code flow, from the authorization request built by the server
 * to the session cookie it answers with, and loads the logged user.
 */
class LoginUseCase @Inject constructor(
    private val sessionManager: SessionManager,
    private val authRepository: AuthRepository,
    private val userDataRepository: UserDataRepository,
    private val db: RoomDatabase,
) {
    suspend operator fun invoke(
        username: String,
        password: String,
    ): Result<UserModel, DataError> = withContext(Dispatchers.IO) {
        Timber.i("invoke: starting login")

        sessionManager
            .login(Credentials(username = username, password = password))
            .getOrElse { err -> return@withContext Err(err) }

        val previousUserId = userDataRepository.prevLoggedUserId.first()
        val newUser = authRepository.fetchLoggedUser().getOrElse { return@withContext Err(it) }

        if (previousUserId != null && previousUserId != newUser.id) {
            Timber.i("invoke: starting DB clear")
            db.clearAllTables()
            Timber.i("invoke: finished DB clear")
        }

        authRepository.finishLogin()
    }
}
