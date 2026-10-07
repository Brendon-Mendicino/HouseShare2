package lol.terabrendon.houseshare2.presentation.navigation

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import lol.terabrendon.houseshare2.data.repository.UserDataRepository
import lol.terabrendon.houseshare2.data.repository.UserDataRepository.Update.BackStack
import lol.terabrendon.houseshare2.domain.auth.AuthState
import timber.log.Timber

class NavigatorImpl(
    private val userDataRepository: UserDataRepository,
    private val coroutineScope: CoroutineScope,
    private val authState: StateFlow<AuthState>,
) : Navigator<MainNavigation> {
    companion object {
        private val LOADING = listOf(MainNavigation.Loading)
        private val LOGIN = listOf(MainNavigation.Login)
        private val LEGAL = listOf(MainNavigation.Legal)
        private val DEFAULT_HOMEPAGE = listOf(HomepageNavigation.Groups)
    }

    private val termsAccepted = userDataRepository
        .termsAndConditions
        .stateIn(coroutineScope, SharingStarted.Eagerly, true)

    // null until the stored back stack is read, so that it is never mistaken for an empty one.
    private val storedBackStack = userDataRepository
        .savedBackStack
        .onEach { Timber.i("storedBackStack: %s", it) }
        .stateIn(coroutineScope, SharingStarted.Eagerly, null)

    private val currentBackStack: List<MainNavigation>
        get() = storedBackStack.value ?: LOADING

    override val backStack: Flow<List<MainNavigation>>
        get() = combine(
            storedBackStack,
            authState,
            termsAccepted,
        ) { backStack, authState, termsAccepted ->
            when {
                !termsAccepted -> LEGAL
                authState is AuthState.Loading || backStack == null -> LOADING
                authState is AuthState.LoggedOut -> LOGIN
                // Logged in, but the stored stack still points to the login (or is empty).
                backStack.all { it == MainNavigation.Login || it == MainNavigation.Loading } -> DEFAULT_HOMEPAGE
                else -> backStack
            }
        }.distinctUntilChanged()
            .onEach { check(it.isNotEmpty()) { "BackStack size must always by greater than 0!" } }
            .onEach { Timber.d("backStack: %s", it) }

    private fun handleNavigationWithGraph(dest: MainNavigation): List<MainNavigation> {
        return if (dest in MainNavigation.topLevelRoutes) {
            listOf(dest)
        } else {
            currentBackStack + dest
        }
    }

    override fun navigate(dest: MainNavigation) {
        coroutineScope.launch {
            userDataRepository.update(BackStack(handleNavigationWithGraph(dest)))
        }
    }

    override fun replace(dest: MainNavigation) {
        coroutineScope.launch {
            val backStack = currentBackStack
            userDataRepository.update(BackStack(backStack.slice(0..backStack.size - 2) + dest))
        }
    }

    override fun pop(elements: Int) {
        check(elements > 0) { "Popped elements should be greater than 0!" }

        coroutineScope.launch {
            val backStack = currentBackStack
            userDataRepository.update(BackStack(backStack.slice(0..backStack.size - 1 - elements)))
        }
    }
}