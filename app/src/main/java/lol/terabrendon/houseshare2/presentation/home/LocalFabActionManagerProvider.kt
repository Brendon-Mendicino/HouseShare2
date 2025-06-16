package lol.terabrendon.houseshare2.presentation.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import lol.terabrendon.houseshare2.presentation.util.FabActionManager
import lol.terabrendon.houseshare2.presentation.util.LocalFabActionManager

/**
 * Provides the [LocalFabActionManager] to all the children inside [content].
 */
@Composable
fun LocalFabActionManagerProvider(content: @Composable ((FabActionManager) -> Unit)) {
    val fabActionManager = remember { FabActionManager() }

    CompositionLocalProvider(LocalFabActionManager provides fabActionManager) {
        content(fabActionManager)
    }
}