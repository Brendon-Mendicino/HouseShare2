package lol.terabrendon.houseshare2.presentation.navigation

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import lol.terabrendon.houseshare2.data.local.preferences.UserData
import lol.terabrendon.houseshare2.data.repository.UserDataRepository
import lol.terabrendon.houseshare2.domain.auth.AuthState
import org.junit.Test

class NavigatorImplTest {

    /**
     * Only what the navigator reads.
     */
    private class FakeUserData(initial: UserData) : UserDataRepository {
        val data_ = MutableStateFlow(initial)
        override val savedBackStack: Flow<List<MainNavigation>> =
            data_.map { it.backStack.ifEmpty { listOf(MainNavigation.Loading) } }
        override val currentLoggedUserId: Flow<Long?> = data_.map { it.currentLoggedUserId }
        override val prevLoggedUserId: Flow<Long?> = data_.map { it.prevLoggedUserId }
        override val selectedGroupId: Flow<Long?> = data_.map { it.selectedGroupId }
        override val termsAndConditions: Flow<Boolean> = data_.map { it.termsAndConditions }
        override val sendAnalytics: Flow<Boolean> = data_.map { it.sendAnalytics }
        override val data: Flow<UserData> = data_
        override suspend fun update(update: UserDataRepository.Update) = Unit
    }

    private val shopping = listOf(HomepageNavigation.Shopping)

    private suspend fun TestScope.backStack(
        auth: AuthState,
        data: UserData = UserData(termsAndConditions = true, backStack = shopping),
    ): List<MainNavigation> {
        val navigator = NavigatorImpl(FakeUserData(data), backgroundScope, MutableStateFlow(auth))
        advanceUntilIdle()
        return navigator.backStack.first()
    }

    @Test
    fun `terms not accepted show the legal screen`() = runTest {
        val res = backStack(
            AuthState.LoggedIn(1),
            UserData(termsAndConditions = false, backStack = shopping)
        )
        assertThat(res).containsExactly(MainNavigation.Legal)
    }

    @Test
    fun `unknown auth state shows the loading screen`() = runTest {
        assertThat(backStack(AuthState.Loading)).containsExactly(MainNavigation.Loading)
    }

    @Test
    fun `logged out shows the login screen`() = runTest {
        assertThat(backStack(AuthState.LoggedOut)).containsExactly(MainNavigation.Login)
    }

    @Test
    fun `logged in shows the stored back stack`() = runTest {
        assertThat(backStack(AuthState.LoggedIn(1))).isEqualTo(shopping)
    }

    @Test
    fun `logged in with the login still stored shows the homepage`() = runTest {
        val res = backStack(
            AuthState.LoggedIn(1),
            UserData(termsAndConditions = true, backStack = listOf(MainNavigation.Login))
        )
        assertThat(res).containsExactly(HomepageNavigation.Groups)
    }
}
