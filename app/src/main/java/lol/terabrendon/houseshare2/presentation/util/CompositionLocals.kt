package lol.terabrendon.houseshare2.presentation.util

import androidx.compose.runtime.compositionLocalOf

val LocalFabActionManager = compositionLocalOf<FabActionManager> {
    error("FabActionManager not provided")
}
