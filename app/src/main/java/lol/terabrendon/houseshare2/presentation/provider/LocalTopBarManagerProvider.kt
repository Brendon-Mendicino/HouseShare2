package lol.terabrendon.houseshare2.presentation.provider

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import lol.terabrendon.houseshare2.presentation.navigation.MainNavigation

const val TAG_TOP: String = "LocalTopBarManagerProvider"

@Composable
fun LocalTopBarManagerProvider(content: @Composable () -> Unit) {
    val topBarManger = remember { TopBarManager() }

    CompositionLocalProvider(LocalTopBarManager provides topBarManger) {
        content()
    }
}

@Composable
inline fun <reified T : MainNavigation> RegisterTopBarConfig(
    config: TopBarConfig,
    enabled: Boolean = true,
) {
    val topBarManager = LocalTopBarManager.current

    // Use the backStack to make dispositions happen quicker
    val backStack = LocalBackStack.current
    val enabled = enabled && backStack?.lastOrNull() is T

    DisposableEffect(config, enabled) {
        if (!enabled)
            return@DisposableEffect onDispose { }

        Log.i(TAG_TOP, "RegisterTopBarConfig: setting-up topBarManager")
        val key = topBarManager.putState(config)

        onDispose {
            topBarManager.removeState(key)
            Log.i(TAG_TOP, "RegisterTopBarConfig: disposing topBarManager")
        }
    }
}