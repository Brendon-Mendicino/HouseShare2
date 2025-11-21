package lol.terabrendon.houseshare2.domain.usecase

import android.net.Uri
import android.util.Log
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.coroutineBinding
import lol.terabrendon.houseshare2.data.remote.api.AuthApi
import lol.terabrendon.houseshare2.data.repository.AuthRepository
import lol.terabrendon.houseshare2.domain.error.DataError
import lol.terabrendon.houseshare2.domain.model.UserModel
import javax.inject.Inject

/**
 * Completes the login process. Before calling this UseCase you need to call
 * [StartLoginUseCase].
 */
class FinishLoginUseCase @Inject constructor(
    private val authApi: AuthApi,
    private val authRepository: AuthRepository,
) {
    companion object {
        const val TAG = "FinishLoginUseCase"
    }

    suspend operator fun invoke(redirectUri: Uri): Result<UserModel, DataError> = coroutineBinding {
        val query = { name: String ->
            redirectUri.getQueryParameter(name)
                ?: throw IllegalStateException("The query parameter param=$name was not present in the redirectUri! redirectUri=$redirectUri")
        }

        Log.i(TAG, "invoke: Finishing login")

        authApi.authCodeFlow(
            state = query("state"),
            sessionState = query("session_state"),
            iss = query("iss"),
            code = query("code"),
        ).bind()

        Log.i(TAG, "invoke: successfully completed oauth2 code flow with server")

        authRepository.finishLogin().bind()
    }
}