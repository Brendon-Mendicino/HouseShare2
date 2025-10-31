package lol.terabrendon.houseshare2.presentation.util

import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.toRoute
import lol.terabrendon.houseshare2.presentation.navigation.MainNavigation

/**
 * Get the current route on the [NavBackStackEntry].
 */
fun NavBackStackEntry.currentRoute(): MainNavigation = MainNavigation
    .subclasses()
    .firstOrNull { destination.hasRoute(it) }
    .let {
        it
            ?: throw RuntimeException("Navigation class not found! You forgot to add ${destination::class.qualifiedName} to MainNavigation::subclasses()")
    }
    .let { toRoute(it) }
