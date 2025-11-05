package lol.terabrendon.houseshare2.presentation.provider

import androidx.compose.runtime.compositionLocalOf

val LocalFabActionManager = compositionLocalOf<FabActionManager> {
    error("FabActionManager not provided")
}

val LocalMenuActionManager = compositionLocalOf<MenuActionManager> {
    error("MenuActionManager not provided")
}

val LocalTopBarManager = compositionLocalOf<TopBarManager> {
    error("TopBarManager not provided")
}