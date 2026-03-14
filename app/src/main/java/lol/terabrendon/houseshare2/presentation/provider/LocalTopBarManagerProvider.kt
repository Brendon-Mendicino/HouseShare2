package lol.terabrendon.houseshare2.presentation.provider

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import lol.terabrendon.houseshare2.presentation.navigation.MainNavigation
import timber.log.Timber
import kotlin.reflect.KClass

@Composable
fun LocalTopBarManagerProvider(content: @Composable () -> Unit) {
    val topBarManger = remember { TopBarManager() }

    CompositionLocalProvider(LocalTopBarManager provides topBarManger) {
        content()
    }
}

@Composable
fun RegisterTopBarConfig(
    config: TopBarConfig,
    route: KClass<out MainNavigation>,
) {
    val topBarManager = LocalTopBarManager.current

    // Use the backStack to make dispositions happen quicker
    val backStack = LocalBackStack.current
    val enabled = backStack?.lastOrNull()?.let { it::class } == route

    DisposableEffect(config, enabled) {
        if (!enabled) return@DisposableEffect onDispose { }

        Timber.d("RegisterTopBarConfig: setting-up topBarManager")
        val key = topBarManager.putState(config)

        onDispose {
            topBarManager.removeState(key)
            Timber.d("RegisterTopBarConfig: disposing topBarManager")
        }
    }
}