package lol.terabrendon.houseshare2.domain.usecase

import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.getErrorOr
import lol.terabrendon.houseshare2.data.remote.api.AuthApi
import lol.terabrendon.houseshare2.data.remote.api.IdpApi
import lol.terabrendon.houseshare2.domain.error.DataError
import lol.terabrendon.houseshare2.domain.error.RemoteError
import timber.log.Timber
import javax.inject.Inject

/**
 * Logs out of the server and of the identity provider, then clears every local trace of the
 * session through [FinishLogoutUseCase].
 *
 * No browser is involved: the provider session lives in the cookie jar of the app, so the app can
 * end it by following the logout url the server answers with.
 */
class StartLogoutUseCase @Inject constructor(
    private val authApi: AuthApi,
    private val idpApi: IdpApi,
    private val finishLogout: FinishLogoutUseCase,
) {
    suspend operator fun invoke(): Result<Unit, DataError> {
        val res = authApi.logout()
        Timber.i("invoke: logout response: %s", res)

        // The server answers with a redirect to the end session endpoint of the provider.
        val redirect = res.getErrorOr(null) as? RemoteError.Redirect

        if (redirect != null) {
            // Best effort: whatever the provider answers, the local session is gone after this
            // use-case, otherwise the user would be stuck logged in.
            runCatching { idpApi.get(redirect.location) }
                .onFailure { e -> Timber.w(e, "invoke: provider logout failed") }
                .onSuccess { Timber.i("invoke: provider session ended") }
        } else {
            Timber.w("invoke: the server did not answer with the provider logout url")
        }

        finishLogout()

        Timber.i("invoke: logout completed")

        return Ok(Unit)
    }
}
