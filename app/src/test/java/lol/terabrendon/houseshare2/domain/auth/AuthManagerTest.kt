package lol.terabrendon.houseshare2.domain.auth

import androidx.datastore.core.DataStore
import androidx.room.RoomDatabase
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import lol.terabrendon.houseshare2.data.local.preferences.UserData
import lol.terabrendon.houseshare2.data.remote.api.AuthApi
import lol.terabrendon.houseshare2.data.remote.api.IdpApi
import lol.terabrendon.houseshare2.data.repository.AuthRepository
import lol.terabrendon.houseshare2.data.repository.SessionManager
import lol.terabrendon.houseshare2.data.repository.UserDataRepositoryImpl
import lol.terabrendon.houseshare2.domain.error.RemoteError
import lol.terabrendon.houseshare2.domain.model.UserModel
import lol.terabrendon.houseshare2.presentation.navigation.HomepageNavigation
import lol.terabrendon.houseshare2.presentation.navigation.MainNavigation
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.inOrder
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever
import org.mockito.kotlin.wheneverBlocking
import retrofit2.Response
import java.net.CookieStore

@OptIn(ExperimentalCoroutinesApi::class)
class AuthManagerTest {

    private class InMemoryDataStore<T>(initial: T) : DataStore<T> {
        val value = MutableStateFlow(initial)
        override val data: Flow<T> = value
        override suspend fun updateData(transform: suspend (t: T) -> T): T =
            transform(value.value).also { value.value = it }
    }

    private val sessionManager = mock<SessionManager>()
    private val authRepository = mock<AuthRepository>()
    private val authApi = mock<AuthApi>()
    private val idpApi = mock<IdpApi>()
    private val db = mock<RoomDatabase>()
    private val cookieStore = mock<CookieStore>()

    private val user = UserModel.default().copy(id = 7, username = "giulia")

    private lateinit var store: InMemoryDataStore<UserData>

    private fun TestScope.authManager(data: UserData = UserData()): AuthManager {
        store = InMemoryDataStore(data)
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        return AuthManager(
            sessionManager = sessionManager,
            authRepository = authRepository,
            authApi = authApi,
            idpApi = idpApi,
            userDataRepository = UserDataRepositoryImpl(store),
            db = db,
            cookieStore = cookieStore,
            // Background: the snackbar of a lost session waits for a UI that tests do not have.
            // Unconfined: advanceUntilIdle does not run background work.
            scope = CoroutineScope(backgroundScope.coroutineContext + dispatcher),
            ioDispatcher = dispatcher,
        )
    }

    private fun stubSuccessfulLogin() {
        wheneverBlocking { sessionManager.login(any()) }.thenReturn(Ok(Unit))
        wheneverBlocking { authRepository.fetchLoggedUser() }.thenReturn(Ok(user))
        wheneverBlocking { authRepository.saveUser(user) }.thenReturn(Ok(Unit))
    }

    @Test
    fun `state is logged out without a stored user`() = runTest {
        val manager = authManager()
        advanceUntilIdle()

        assertThat(manager.state.value).isEqualTo(AuthState.LoggedOut)
    }

    @Test
    fun `state is logged in with a stored user, without asking the server`() = runTest {
        val manager = authManager(UserData(currentLoggedUserId = 7))
        advanceUntilIdle()

        assertThat(manager.state.value).isEqualTo(AuthState.LoggedIn(7))
        verifyNoInteractions(authRepository, sessionManager, authApi)
    }

    @Test
    fun `login of the same user keeps the data and leaves the login screen`() = runTest {
        val manager =
            authManager(UserData(prevLoggedUserId = 7, backStack = listOf(MainNavigation.Login)))
        stubSuccessfulLogin()

        val res = manager.login("giulia", "secret")
        advanceUntilIdle()

        assertThat(res).isEqualTo(Ok(user))
        assertThat(manager.state.value).isEqualTo(AuthState.LoggedIn(7))
        assertThat(store.value.value.prevLoggedUserId).isEqualTo(7)
        assertThat(store.value.value.backStack).containsExactly(HomepageNavigation.Groups)
        verify(db, never()).clearAllTables()
        verify(authRepository, times(1)).fetchLoggedUser()
    }

    @Test
    fun `login of a different user clears the data of the previous one first`() = runTest {
        val manager = authManager(UserData(prevLoggedUserId = 3))
        stubSuccessfulLogin()

        manager.login("giulia", "secret")

        inOrder(db, authRepository) {
            verify(db).clearAllTables()
            verify(authRepository).saveUser(user)
        }
        assertThat(store.value.value.prevLoggedUserId).isEqualTo(7)
    }

    @Test
    fun `failed login does not touch the stored session`() = runTest {
        val manager = authManager(UserData(prevLoggedUserId = 3))
        wheneverBlocking { sessionManager.login(any()) }.thenReturn(Ok(Unit))
        wheneverBlocking { authRepository.fetchLoggedUser() }.thenReturn(Err(RemoteError.NoConnection))

        val res = manager.login("giulia", "secret")
        advanceUntilIdle()

        assertThat(res).isEqualTo(Err(RemoteError.NoConnection))
        assertThat(manager.state.value).isEqualTo(AuthState.LoggedOut)
        assertThat(store.value.value.prevLoggedUserId).isEqualTo(3)
        verify(db, never()).clearAllTables()
    }

    @Test
    fun `logout ends the provider session and wipes everything`() = runTest {
        val manager = authManager(
            UserData(
                currentLoggedUserId = 7,
                prevLoggedUserId = 7,
                selectedGroupId = 1
            )
        )
        wheneverBlocking { authApi.logout() }
            .thenReturn(Err(RemoteError.Redirect(mock<Response<*>>(), "https://idp/logout")))
        wheneverBlocking { idpApi.get("https://idp/logout") }.thenReturn(mock())

        manager.logout()
        advanceUntilIdle()

        verify(idpApi).get("https://idp/logout")
        verify(cookieStore).removeAll()
        verify(db).clearAllTables()
        assertThat(manager.state.value).isEqualTo(AuthState.LoggedOut)
        assertThat(store.value.value.selectedGroupId).isNull()
        assertThat(store.value.value.backStack).containsExactly(MainNavigation.Login)
    }

    @Test
    fun `lost session clears the session but keeps the data`() = runTest {
        val manager = authManager(
            UserData(
                currentLoggedUserId = 7,
                prevLoggedUserId = 7,
                selectedGroupId = 1
            )
        )
        advanceUntilIdle()
        whenever(sessionManager.generation).thenReturn(4)

        manager.onSessionLost(generation = 4)
        advanceUntilIdle()

        verify(cookieStore).removeAll()
        verify(db, never()).clearAllTables()
        assertThat(manager.state.value).isEqualTo(AuthState.LoggedOut)
        assertThat(store.value.value.prevLoggedUserId).isEqualTo(7)
        assertThat(store.value.value.selectedGroupId).isEqualTo(1)
    }

    @Test
    fun `lost session of an old generation is ignored`() = runTest {
        val manager = authManager(UserData(currentLoggedUserId = 7))
        advanceUntilIdle()
        whenever(sessionManager.generation).thenReturn(5)

        manager.onSessionLost(generation = 4)
        advanceUntilIdle()

        verifyNoInteractions(cookieStore)
        assertThat(manager.state.value).isEqualTo(AuthState.LoggedIn(7))
    }

    @Test
    fun `lost session while logged out is ignored`() = runTest {
        val manager = authManager()
        advanceUntilIdle()
        whenever(sessionManager.generation).thenReturn(4)

        manager.onSessionLost(generation = 4)
        advanceUntilIdle()

        verifyNoInteractions(cookieStore)
    }
}
