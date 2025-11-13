package lol.terabrendon.houseshare2.presentation.navigation

import android.util.Log
import com.github.michaelbull.result.mapBoth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import lol.terabrendon.houseshare2.data.repository.AuthRepository
import lol.terabrendon.houseshare2.data.repository.UserDataRepository
import kotlin.time.DurationUnit
import kotlin.time.toDuration

class NavigatorImpl(
    private val userDataRepository: UserDataRepository,
    private val coroutineScope: CoroutineScope,
    private val authRepository: AuthRepository,
) : Navigator<MainNavigation> {
    companion object {
        private const val TAG = "NavigatorImpl"

        private val LOADING = listOf(MainNavigation.Loading)
        private val LOGIN = listOf(MainNavigation.Login)
    }

    private val isLoading = MutableStateFlow(true)

    @OptIn(ExperimentalCoroutinesApi::class)
    // Listen to any change in the current logged-in user
    // If the user changed reinitiate the login check
    private val isLoggedIn = userDataRepository.currentLoggedUserId
        .flatMapLatest {
            isLoading.value = true

            flow {
                while (true) {
                    Log.i(TAG, "isLoggedIn: check if user is login state")
                    val res = authRepository.loggedUser().mapBoth(
                        success = { true },
                        failure = { false }
                    )

                    emit(res)
                    isLoading.value = false

                    // Check again in 5 minutes if the user is still logged-in
                    delay(5.toDuration(DurationUnit.MINUTES))
                }
            }
        }
        .onEach { isLoggedIn ->
            if (isLoggedIn)
                Log.i(TAG, "isLoggedIn: user is logged-in")
            else
                Log.i(TAG, "isLoggedIn: user is logged-out")
        }
        .stateIn(coroutineScope, SharingStarted.Eagerly, false)

    private val _backStack = userDataRepository
        .savedBackStack
        .onEach { Log.i(TAG, "_backStack: $it") }
        .stateIn(coroutineScope, SharingStarted.Eagerly, listOf(MainNavigation.Loading))

    override val backStack: Flow<List<MainNavigation>>
        get() = combine(_backStack, isLoading, isLoggedIn) { backStack, isLoading, isLoggedIn ->
            if (isLoading)
                LOADING
            else if (!isLoggedIn)
                LOGIN
            else if (backStack == LOGIN || backStack == LOADING) {
                userDataRepository.updateBackStack(listOf(HomepageNavigation.Groups))
                LOGIN
            } else
                backStack
        }
            .distinctUntilChanged()
            .onEach { check(it.isNotEmpty()) { "BackStack size must always by greater than 0!" } }

    private fun handleNavigationWithGraph(dest: MainNavigation): List<MainNavigation> {
        return if (dest in MainNavigation.topLevelRoutes) {
            listOf(dest)
        } else {
            _backStack.value + dest
        }
    }

    override fun navigate(dest: MainNavigation) {
        coroutineScope.launch {
            userDataRepository.updateBackStack(
                handleNavigationWithGraph(dest)
            )
        }
    }

    override fun replace(dest: MainNavigation) {
        coroutineScope.launch {
            val backStack = _backStack.value
            userDataRepository.updateBackStack(
                backStack.slice(0..backStack.size - 2) + dest
            )
        }
    }

    override fun pop(elements: Int) {
        check(elements > 0) { "Popped elements should be greater than 0!" }

        coroutineScope.launch {
            val backStack = _backStack.value
            userDataRepository.updateBackStack(
                backStack.slice(0..backStack.size - 1 - elements)
            )
        }
    }
}