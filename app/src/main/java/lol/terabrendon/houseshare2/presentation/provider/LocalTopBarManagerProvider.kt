package lol.terabrendon.houseshare2.presentation.provider

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember

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

    DisposableEffect(config, enabled) {
        if (!enabled)
            return@DisposableEffect onDispose { }

        Log.i(TAG, "RegisterTopBarConfig: setting-up topBarManager")
        topBarManager.setConfig(config)

        onDispose {
            topBarManager.resetConfig()
            Log.i(TAG, "RegisterTopBarConfig: disposing topBarManager")
        }
    }
}