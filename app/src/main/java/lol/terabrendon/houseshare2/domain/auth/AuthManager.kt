package lol.terabrendon.houseshare2.domain.auth

import androidx.room.RoomDatabase
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.getErrorOr
import com.github.michaelbull.result.getOrElse
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import lol.terabrendon.houseshare2.data.remote.api.AuthApi
import lol.terabrendon.houseshare2.data.remote.api.IdpApi
import lol.terabrendon.houseshare2.data.repository.AuthRepository
import lol.terabrendon.houseshare2.data.repository.SessionManager
import lol.terabrendon.houseshare2.data.repository.UserDataRepository
import lol.terabrendon.houseshare2.data.repository.UserDataRepository.Update.BackStack
import lol.terabrendon.houseshare2.data.repository.UserDataRepository.Update.LoggedUserId
import lol.terabrendon.houseshare2.data.repository.UserDataRepository.Update.PrevLoggedUserId
import lol.terabrendon.houseshare2.data.repository.UserDataRepository.Update.SelectedGroupId
import lol.terabrendon.houseshare2.data.util.DataResult
import lol.terabrendon.houseshare2.di.IoDispatcher
import lol.terabrendon.houseshare2.domain.error.RemoteError
import lol.terabrendon.houseshare2.domain.model.UserModel
import lol.terabrendon.houseshare2.presentation.navigation.HomepageNavigation
import lol.terabrendon.houseshare2.presentation.navigation.MainNavigation
import lol.terabrendon.houseshare2.presentation.util.SnackbarController
import timber.log.Timber
import java.net.CookieStore
import javax.inject.Inject
import javax.inject.Singleton

/**
 * The only owner of the login state: logs in, logs out, and reacts when the server session is lost.
 *
 * The logged user is the one stored in the [UserDataRepository], which is the only thing [state]
 * is derived from: the app is offline-first, so a stored user counts as logged in without asking
 * the server. A dead session is noticed by [lol.terabrendon.houseshare2.data.remote.interceptor.SessionRenewInterceptor]
 * when a real request fails and cannot be renewed, which calls [onSessionLost].
 *
 * Nothing else writes the logged user ids. Every write goes through [mutex], so a logout can not
 * be undone by a login or a session loss finishing later.
 */
@Singleton
class AuthManager @Inject constructor(
    private val sessionManager: SessionManager,
    private val authRepository: AuthRepository,
    private val authApi: AuthApi,
    private val idpApi: IdpApi,
    private val userDataRepository: UserDataRepository,
    private val db: RoomDatabase,
    private val cookieStore: CookieStore,
    private val scope: CoroutineScope,
    @IoDispatcher
    private val ioDispatcher: CoroutineDispatcher,
) {
    private val mutex = Mutex()

    val state: StateFlow<AuthState> = userDataRepository
        .currentLoggedUserId
        .map { userId -> if (userId == null) AuthState.LoggedOut else AuthState.LoggedIn(userId) }
        .stateIn(scope, SharingStarted.Eagerly, AuthState.Loading)

    /**
     * Runs the whole code flow with the credentials typed by the user, then stores the user. When a
     * different user than the previous one logs in, the data of the previous one is deleted.
     */
    suspend fun login(username: String, password: String): DataResult<UserModel> =
        withContext(ioDispatcher) {
            mutex.withLock {
                Timber.i("login: starting login")

                sessionManager
                    .login(Credentials(username = username, password = password))
                    .getOrElse { err -> return@withLock Err(err) }

                val user = authRepository.fetchLoggedUser()
                    .getOrElse { err -> return@withLock Err(err) }

                val previousUserId = userDataRepository.prevLoggedUserId.first()
                if (previousUserId != null && previousUserId != user.id) {
                    Timber.i("login: a different user logged in, clearing the DB")
                    db.clearAllTables()
                }

                authRepository.saveUser(user).getOrElse { err -> return@withLock Err(err) }

                userDataRepository.update(PrevLoggedUserId(user.id))
                userDataRepository.update(LoggedUserId(user.id))

                // Leave the login screen for the homepage. A fresh installation has no back stack at all.
                val backStack = userDataRepository.savedBackStack.first()
                if (backStack.all { it == MainNavigation.Login || it == MainNavigation.Loading }) {
                    userDataRepository.update(BackStack(listOf(HomepageNavigation.Groups)))
                }

                Timber.i("login: logged in. user=%s", user)
                Ok(user)
            }
        }

    /**
     * Logs out of the server and of the identity provider, then clears every local trace of the
     * session.
     *
     * No browser is involved: the provider session lives in the cookie jar of the app, so the app can
     * end it by following the logout url the server answers with.
     */
    suspend fun logout(): DataResult<Unit> = withContext(ioDispatcher) {
        val res = authApi.logout()
        Timber.i("logout: logout response: %s", res)

        // The server answers with a redirect to the end session endpoint of the provider.
        val redirect = res.getErrorOr(null) as? RemoteError.Redirect

        if (redirect != null) {
            // Best effort: whatever the provider answers, the local session is gone after this
            // function, otherwise the user would be stuck logged in.
            runCatching { idpApi.get(redirect.location) }
                .onFailure { e -> Timber.w(e, "logout: provider logout failed") }
                .onSuccess { Timber.i("logout: provider session ended") }
        } else {
            Timber.w("logout: the server did not answer with the provider logout url")
        }

        clearSession(wipeData = true)

        Ok(Unit)
    }

    /**
     * Forgets the session locally, without telling the server.
     *
     * @param wipeData also delete the stored data and the selected group. When `false` the same user
     * logging back in finds everything where it was.
     */
    suspend fun clearSession(wipeData: Boolean) = withContext(ioDispatcher) {
        mutex.withLock { clearSessionLocked(wipeData) }
    }

    /**
     * The server session is gone and the provider wants the credentials again. Called from the http
     * client when a request fails with no way to renew the session.
     *
     * @param generation the [SessionManager.generation] the failed request was sent with: if a new
     * session was opened in the meantime the call is stale and ignored.
     */
    fun onSessionLost(generation: Long) {
        scope.launch(ioDispatcher) {
            mutex.withLock {
                if (generation != sessionManager.generation) {
                    Timber.i("onSessionLost: a new session was opened in the meantime, ignoring")
                    return@withLock
                }
                if (state.value !is AuthState.LoggedIn) {
                    return@withLock
                }

                Timber.w("onSessionLost: the session expired, the user has to log in again")
                clearSessionLocked(wipeData = false)
            }

            SnackbarController.sendError(RemoteError.NoSession)
        }
    }

    private suspend fun clearSessionLocked(wipeData: Boolean) {
        if (wipeData) {
            db.clearAllTables()
            userDataRepository.update(SelectedGroupId(null))
        }

        // Both sessions live here, the one of the server and the one of the identity provider.
        // They used to survive the logout, which made the next login start half authenticated.
        cookieStore.removeAll()

        userDataRepository.update(LoggedUserId(null))
        userDataRepository.update(BackStack(listOf(MainNavigation.Login)))

        Timber.i("clearSession: session cleared, wipeData=%s", wipeData)
    }
}
