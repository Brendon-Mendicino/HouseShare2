package lol.terabrendon.houseshare2.presentation.navigation

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import lol.terabrendon.houseshare2.data.repository.AuthRepository
import lol.terabrendon.houseshare2.data.repository.UserDataRepository

class NavigatorImpl(
    private val userDataRepository: UserDataRepository,
    private val coroutineScope: CoroutineScope,
    private val authRepository: AuthRepository,
) : Navigator<MainNavigation> {
    companion object {
        private const val TAG = "NavigatorImpl"
    }

    private val _backStack = userDataRepository
        .savedBackStack
        .onEach { Log.i(TAG, "_backStack: $it") }
        .stateIn(coroutineScope, SharingStarted.Eagerly, listOf(MainNavigation.Loading))

    override val backStack: Flow<List<MainNavigation>>
        get() = _backStack
            .onEach { check(it.isNotEmpty()) { "BackStack size must always by greater than 0!" } }

    init {
        coroutineScope.launch {
            val backStack = userDataRepository.savedBackStack.first()

            // TODO: change this in the future with login check ecc...
            if (backStack == listOf(MainNavigation.Loading) || authRepository.loggedUser().isErr) {
                userDataRepository.updateBackStack(listOf(MainNavigation.Login))
            }
        }
    }

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