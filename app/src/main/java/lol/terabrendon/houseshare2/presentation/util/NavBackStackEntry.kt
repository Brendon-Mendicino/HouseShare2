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
    .first { destination.hasRoute(it) }
    .let { toRoute(it) }
