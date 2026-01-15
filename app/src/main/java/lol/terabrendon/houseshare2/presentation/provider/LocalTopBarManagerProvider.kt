package lol.terabrendon.houseshare2.presentation.provider

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import lol.terabrendon.houseshare2.presentation.navigation.MainNavigation
import timber.log.Timber

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

        Timber.d("RegisterTopBarConfig: setting-up topBarManager")
        val key = topBarManager.putState(config)

        onDispose {
            topBarManager.removeState(key)
            Timber.d("RegisterTopBarConfig: disposing topBarManager")
        }
    }
}