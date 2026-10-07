package lol.terabrendon.houseshare2.data.repository

import androidx.core.net.toUri
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.getOrElse
import com.github.michaelbull.result.map
import com.github.michaelbull.result.onFailure
import com.github.michaelbull.result.unwrapError
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import lol.terabrendon.houseshare2.data.remote.api.AuthApi
import lol.terabrendon.houseshare2.data.remote.api.NetResult
import lol.terabrendon.houseshare2.data.remote.util.convertResponse
import lol.terabrendon.houseshare2.domain.auth.Credentials
import lol.terabrendon.houseshare2.domain.auth.IdpAuthenticator
import lol.terabrendon.houseshare2.domain.error.RemoteError
import timber.log.Timber
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Owns the app session.
 *
 * The whole code flow runs through the http client of the app, so both sessions it produces, the
 * one of the server and the one of the identity provider, end up in the persisted cookie jar.
 * That is what makes [renew] possible: the server session is short lived, but as long as the
 * provider still recognises its own cookie, a new one is obtained without asking anything to the
 * user.
 */
@Singleton
class SessionManager @Inject constructor(
    private val authApi: AuthApi,
    private val idpAuthenticator: IdpAuthenticator,
) {
    private val mutex = Mutex()

    /**
     * Incremented every time a new session is opened. A request remembers the value it was sent
     * with, so that when it fails it is possible to tell whether the session it used is still the
     * current one.
     */
    @Volatile
    var generation: Long = 0
        private set

    /**
     * Opens a session with the credentials typed by the user.
     */
    suspend fun login(credentials: Credentials): NetResult<Unit> = mutex.withLock {
        connectionSafe { authenticate(credentials) }
    }

    /**
     * Opens a new session reusing the provider session stored in the cookie jar.
     *
     * Many calls fail at once when the session expires: [staleGeneration] is the [generation] the
     * failed call was sent with, and when another call already renewed the session in the
     * meantime nothing is done.
     *
     * Fails with [RemoteError.NoSession] when the provider wants the credentials again, which is
     * the only case where the user has to go back to the login screen.
     */
    suspend fun renew(staleGeneration: Long): NetResult<Unit> = mutex.withLock {
        if (generation != staleGeneration) {
            Timber.i("renew: the session was already renewed")
            return@withLock Ok(Unit)
        }

        Timber.i("renew: renewing the session")

        connectionSafe { authenticate(credentials = null) }
    }

    /**
     * Where a new user can sign up. The url is built from a fresh authorization request of the
     * server, so the app still knows nothing about the provider configuration.
     */
    suspend fun registrationUrl(): NetResult<String> = mutex.withLock {
        connectionSafe { authorizationUrl().map { idpAuthenticator.registrationUrl(it) } }
    }

    /**
     * The calls of the flow return the raw [retrofit2.Response] to read its redirects, so a failed
     * connection surfaces as an exception instead of a [RemoteError].
     */
    private inline fun <T> connectionSafe(block: () -> NetResult<T>): NetResult<T> = try {
        block()
    } catch (e: IOException) {
        Timber.w(e, "connectionSafe: could not reach the server or the identity provider")
        Err(RemoteError.NoConnection)
    }

    private suspend fun authenticate(credentials: Credentials?): NetResult<Unit> {
        val authorizationUrl = authorizationUrl().getOrElse { err -> return Err(err) }

        val redirect = idpAuthenticator
            .authenticate(authorizationUrl = authorizationUrl, credentials = credentials)
            .getOrElse { err -> return Err(err) }

        // The redirect is addressed to the app, the server is the one that has to see it: it
        // holds the state of the request and it is the one exchanging the code.
        val params = redirect.toUri().let { uri ->
            uri.queryParameterNames.mapNotNull { name ->
                uri.getQueryParameter(name)?.let { value -> name to value }
            }
        }.toMap()

        authApi.authCodeFlow(params).onFailure { err ->
            // The server answers the callback with a redirect to the page the user was going to,
            // which is a success as far as the app is concerned.
            if (err !is RemoteError.Redirect) {
                Timber.w("authenticate: the server refused the authorization code, err=%s", err)
                return Err(err)
            }
        }

        Timber.i("authenticate: a new server session is active")
        generation++

        return Ok(Unit)
    }

    /**
     * Asks the server where the user has to authenticate. The server builds the request, so the
     * app has no knowledge of the provider configuration.
     */
    private suspend fun authorizationUrl(): NetResult<String> {
        val res = authApi.login()

        if (res.isSuccessful) {
            Timber.e("authorizationUrl: the server did not answer with a redirect")
            return Err(RemoteError.Unknown(res))
        }

        val err = convertResponse(res).unwrapError()
        if (err !is RemoteError.Redirect) {
            return Err(err)
        }

        return Ok(err.location)
    }
}
