package lol.terabrendon.houseshare2.presentation.navigation

import com.github.michaelbull.result.mapBoth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import lol.terabrendon.houseshare2.data.repository.AuthRepository
import lol.terabrendon.houseshare2.data.repository.UserDataRepository
import lol.terabrendon.houseshare2.data.repository.UserDataRepository.Update.BackStack
import lol.terabrendon.houseshare2.data.util.NetworkMonitor
import lol.terabrendon.houseshare2.domain.error.RemoteError
import timber.log.Timber
import kotlin.time.Duration.Companion.minutes

class NavigatorImpl(
    private val userDataRepository: UserDataRepository,
    private val coroutineScope: CoroutineScope,
    private val authRepository: AuthRepository,
    networkMonitor: NetworkMonitor,
) : Navigator<MainNavigation> {
    companion object {
        private val LOADING = listOf(MainNavigation.Loading)
        private val LOGIN = listOf(MainNavigation.Login)
        private val LEGAL = listOf(MainNavigation.Legal)
        private val DEFAULT_HOMEPAGE = listOf(HomepageNavigation.Groups)
    }

    private val isLoading = MutableStateFlow(true)

    private val termsAccepted = userDataRepository
        .termsAndConditions
        .stateIn(coroutineScope, SharingStarted.Eagerly, true)

    @OptIn(ExperimentalCoroutinesApi::class)
    // Listen to any change in the current logged-in user
    // If the user changed reinitiate the login check
    private val isLoggedIn = userDataRepository
        .currentLoggedUserId
        .onEach {
            // If no user is currently logged, we are waiting for
            // a server response.
            if (it == null) {
                isLoading.value = true
            }
        }
        .combine(networkMonitor.isOnline) { a, b -> a to b }
        .flatMapLatest { (userId, isOnline) ->
            flow {
                // If a user is present in the DB but we are offline our status must be
                // loggedIn = true
                if (userId != null && isOnline) {
                    emit(true)
                    return@flow
                }

                // Otherwise stay in a pending state checking from the remote server.
                while (true) {
                    Timber.i("isLoggedIn: check if user is login state")
                    // TODO: print other kind of errors
                    val res = authRepository.loggedUser().mapBoth(
                        success = { true },
                        failure = { err -> err !is RemoteError.Unauthorized },
                    )

                    emit(res)

                    // Check again in 5 minutes if the user is still logged-in
                    delay(5.minutes)
                }
            }
        }
        .onEach { isLoading.value = false }
        .distinctUntilChanged()
        .onEach { isLoggedIn ->
            if (isLoggedIn)
                Timber.i("isLoggedIn: user is logged-in")
            else
                Timber.i("isLoggedIn: user is logged-out")
        }
        .stateIn(coroutineScope, SharingStarted.Eagerly, false)

    init {
        coroutineScope.launch {
            // Listen on login changes, if we are logged in and we are still
            // on the LOGIN page change it to the default one in the homepage.
            isLoggedIn.collect { isLogged ->
                val backStack = userDataRepository.savedBackStack.first()
                if (isLogged && (backStack == LOGIN || backStack == LOADING)) {
                    userDataRepository.update(BackStack(DEFAULT_HOMEPAGE))
                }
            }
        }
    }

    private val _backStack = userDataRepository
        .savedBackStack
        .onEach { Timber.i("_backStack: %s", it) }
        .stateIn(coroutineScope, SharingStarted.Eagerly, listOf(MainNavigation.Loading))

    override val backStack: Flow<List<MainNavigation>>
        get() = combine(
            _backStack,
            isLoading,
            isLoggedIn,
            termsAccepted
        ) { backStack, isLoading, isLoggedIn, termsAccepted ->
            when {
                !termsAccepted -> LEGAL
                isLoading -> LOADING
                !isLoggedIn -> LOGIN
                else -> backStack
            }
        }.distinctUntilChanged()
            .onEach { check(it.isNotEmpty()) { "BackStack size must always by greater than 0!" } }
            .onEach { Timber.i("backStack: %s", it) }

    private fun handleNavigationWithGraph(dest: MainNavigation): List<MainNavigation> {
        return if (dest in MainNavigation.topLevelRoutes) {
            listOf(dest)
        } else {
            _backStack.value + dest
        }
    }

    override fun navigate(dest: MainNavigation) {
        coroutineScope.launch {
            userDataRepository.update(BackStack(handleNavigationWithGraph(dest)))
        }
    }

    override fun replace(dest: MainNavigation) {
        coroutineScope.launch {
            val backStack = _backStack.value
            userDataRepository.update(BackStack(backStack.slice(0..backStack.size - 2) + dest))
        }
    }

    override fun pop(elements: Int) {
        check(elements > 0) { "Popped elements should be greater than 0!" }

        coroutineScope.launch {
            val backStack = _backStack.value
            userDataRepository.update(BackStack(backStack.slice(0..backStack.size - 1 - elements)))
        }
    }
}