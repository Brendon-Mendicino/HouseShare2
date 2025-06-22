package lol.terabrendon.houseshare2.presentation.provider

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember

private const val TAG: String = "LocalFabActionManagerProvider"

/**
 * Provides the [LocalFabActionManager] to all the children inside [content].
 *
 * All children can register an action on the fab using [RegisterFabAction].
 */
@Composable
fun LocalFabActionManagerProvider(content: @Composable ((FabActionManager) -> Unit)) {
    val fabActionManager = remember { FabActionManager() }

    CompositionLocalProvider(LocalFabActionManager provides fabActionManager) {
        content(fabActionManager)
    }
}

@Composable
fun RegisterFabAction(action: () -> Unit) {
    val fabActionManager = LocalFabActionManager.current

    LaunchedEffect(Unit) {
        Log.i(TAG, "RegisterFabAction: setting-up fabActionManager")
        fabActionManager.setFabAction {
            action()
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            Log.d(TAG, "RegisterFabAction: re-setting fabActionManager")
            fabActionManager.setFabAction(null)
        }
    }
}