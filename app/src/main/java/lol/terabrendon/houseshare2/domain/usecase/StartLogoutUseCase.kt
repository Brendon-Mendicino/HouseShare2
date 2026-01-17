package lol.terabrendon.houseshare2.domain.usecase

import android.content.Intent
import androidx.core.net.toUri
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.coroutineBinding
import com.github.michaelbull.result.getErrorOr
import lol.terabrendon.houseshare2.DeepLinkActivity
import lol.terabrendon.houseshare2.data.remote.api.AuthApi
import lol.terabrendon.houseshare2.domain.error.DataError
import lol.terabrendon.houseshare2.domain.error.RemoteError
import lol.terabrendon.houseshare2.presentation.util.ActivityQueue
import lol.terabrendon.houseshare2.util.setQuery
import timber.log.Timber
import javax.inject.Inject

/**
 * This use-case starts the logout procedure. This function will make a call the OICD server
 * asking to logout the current session.
 *
 * After the logout we will be redirected to the [DeepLinkActivity] where the request will be
 * handled by [FinishLogoutUseCase].
 */
class StartLogoutUseCase @Inject constructor(
    private val authApi: AuthApi,
) {
    suspend operator fun invoke(): Result<Unit, DataError> = coroutineBinding {
        val res = authApi.logout()
        Timber.i("invoke: logout response: %s", res)

        val redirect = res.getErrorOr(null)
        val uri = when (redirect) {
            is RemoteError.Found -> redirect.location.toUri()
            else -> null
        }

        if (uri == null) {
            Timber.w("invoke: logout failed! response=%s", res)
            return@coroutineBinding
        }

        if (uri.path == "/") {
            Timber.w("we are already logged out! Start an intent with the app logout uri, in this way ${DeepLinkActivity::class.simpleName} will the logout on its own.")

            val intent =
                Intent(Intent.ACTION_VIEW, "app://lol.terabrendon.houseshare2/logout".toUri())
            ActivityQueue.sendIntent(intent)

            return@coroutineBinding
        }

        val logoutUri = uri
            .setQuery("post_logout_redirect_uri", "app://lol.terabrendon.houseshare2/logout")

        val intent = Intent(Intent.ACTION_VIEW, logoutUri)
        ActivityQueue.sendIntent(intent)

        Timber.i("invoke: started logout procedure")
    }
}