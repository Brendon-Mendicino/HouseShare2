package lol.terabrendon.houseshare2.presentation.provider

import androidx.compose.runtime.compositionLocalOf
import lol.terabrendon.houseshare2.presentation.navigation.MainNavigation

val LocalFabManager = compositionLocalOf<FabManager> {
    error("FabActionManager not provided")
}

val LocalMenuActionManager = compositionLocalOf<MenuActionManager> {
    error("MenuActionManager not provided")
}

val LocalTopBarManager = compositionLocalOf<TopBarManager> {
    error("TopBarManager not provided")
}

val LocalBackStack = compositionLocalOf<List<MainNavigation>?> { null }