package lol.terabrendon.houseshare2.presentation.provider

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

private const val TAG: String = "LocalTopBarManagerProvider"

@Composable
fun LocalTopBarManagerProvider(content: @Composable () -> Unit) {
    val topBarManger = remember { TopBarManager() }

    CompositionLocalProvider(LocalTopBarManager provides topBarManger) {
        content()
    }
}

@Composable
fun RegisterTopBarConfig(config: TopBarConfig, enabled: Boolean = true) {
    val topBarManager = LocalTopBarManager.current

    // Use the backStack to make dispositions happen quicker
    val backStack = LocalBackStack.current
    var backStackChanged by remember { mutableIntStateOf(0) }

    if (backStack != null) {
        LaunchedEffect(backStack) {
            backStackChanged += 1
        }
    }

    DisposableEffect(config, enabled, backStackChanged) {
        if (!enabled || backStackChanged > 1)
            return@DisposableEffect onDispose { }

        Log.i(TAG, "RegisterTopBarConfig: setting-up topBarManager")
        topBarManager.setState(config)

        onDispose {
            topBarManager.resetState()
            Log.i(TAG, "RegisterTopBarConfig: disposing topBarManager")
        }
    }
}