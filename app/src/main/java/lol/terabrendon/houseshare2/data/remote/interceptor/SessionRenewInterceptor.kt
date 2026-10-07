package lol.terabrendon.houseshare2.data.remote.interceptor

import androidx.core.net.toUri
import com.github.michaelbull.result.onFailure
import dagger.Lazy
import kotlinx.coroutines.runBlocking
import lol.terabrendon.houseshare2.data.repository.SessionManager
import lol.terabrendon.houseshare2.domain.auth.AuthManager
import lol.terabrendon.houseshare2.domain.error.RemoteError
import okhttp3.Interceptor
import okhttp3.Response
import timber.log.Timber

/**
 * Renews the session when the server answers that there is none, and replays the request once.
 *
 * Without this, an expired session surfaces as an error to the user and is only noticed by the
 * login poller, up to a couple of minutes later.
 *
 * When the provider wants the credentials again the session can not be renewed: [AuthManager] is
 * told, and the user goes back to the login screen.
 *
 * [SessionManager] and [AuthManager] are injected lazily: they depend on the auth and provider APIs,
 * which are built by the same Hilt module that builds the client this interceptor belongs to.
 */
class SessionRenewInterceptor(
    private val sessionManager: Lazy<SessionManager>,
    private val authManager: Lazy<AuthManager>,
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val generation = sessionManager.get().generation
        val response = chain.proceed(request)

        if (!response.isMissingSession()) {
            return response
        }

        Timber.w("intercept: no session for %s, renewing", request.url.encodedPath)

        var renewed = true
        runBlocking {
            sessionManager.get().renew(staleGeneration = generation).onFailure { err ->
                Timber.w("intercept: session renewal failed, err=%s", err)
                renewed = false

                // Only a provider asking for the credentials ends the session: a network error
                // while renewing does not say anything about it.
                if (err is RemoteError.NoSession) {
                    authManager.get().onSessionLost(generation)
                }
            }
        }

        if (!renewed) {
            return response
        }

        response.close()

        // The cookie jar now holds the new session cookie.
        return chain.proceed(request.newBuilder().build())
    }

    /**
     * The server answers 401 on the api paths, and redirects to the login page everywhere else. The
     * client does not follow redirects, so both shapes arrive here.
     */
    private fun Response.isMissingSession(): Boolean {
        if (code == 401) return true

        if (code !in 300..399) return false

        val location = header("Location") ?: return false

        return location.toUri().path == "/login"
    }
}