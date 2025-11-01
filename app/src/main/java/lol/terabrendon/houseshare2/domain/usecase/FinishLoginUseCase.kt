package lol.terabrendon.houseshare2.domain.usecase

import android.net.Uri
import android.util.Log
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.runSuspendCatching
import com.github.michaelbull.result.onFailure
import lol.terabrendon.houseshare2.data.remote.api.LoginApi
import lol.terabrendon.houseshare2.data.repository.AuthRepository
import lol.terabrendon.houseshare2.domain.model.UserModel
import javax.inject.Inject

/**
 * Completes the login process. Before calling this UseCase you need to call
 * [StartLoginUseCase].
 */
class FinishLoginUseCase @Inject constructor(
    private val loginApi: LoginApi,
    private val authRepository: AuthRepository,
) {
    companion object {
        const val TAG = "FinishLoginUseCase"
    }

    suspend operator fun invoke(redirectUri: Uri): Result<UserModel, Throwable> {
        val query = { name: String ->
            redirectUri.getQueryParameter(name)
                ?: throw IllegalStateException("The query parameter param=$name was not present in the redirectUri! redirectUri=$redirectUri")
        }

        Log.i(TAG, "invoke: Finishing login")

        runSuspendCatching {
            loginApi.authCodeFlow(
                state = query("state"),
                sessionState = query("session_state"),
                iss = query("iss"),
                code = query("code"),
            )
        }.onFailure { return Err(it) }

        Log.i(TAG, "invoke: successfully completed oauth2 code flow with server")

        return authRepository.finishLogin()
    }
}