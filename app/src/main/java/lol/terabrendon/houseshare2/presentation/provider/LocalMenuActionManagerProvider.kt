package lol.terabrendon.houseshare2.presentation.provider

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember

private const val TAG: String = "LocalMenuActionManagerProvider"

@Composable
fun LocalMenuActionManagerProvider(content: @Composable (MenuActionManager) -> Unit) {
    val menuAction = remember { MenuActionManager() }

    CompositionLocalProvider(LocalMenuActionManager provides menuAction) {
        content(menuAction)
    }
}

@Composable
fun RegisterMenuAction(key: String, content: @Composable () -> Unit) {
    val menuAction = LocalMenuActionManager.current

    LaunchedEffect(Unit) {
        Log.i(TAG, "RegisterFabAction: setting-up fabActionManager")
        if (menuAction.actions.contains(key)) {
            val msg = "RegisterMenuAction: the same key=$key was registered more than once!"
            Log.e(TAG, msg)
            throw IllegalStateException(msg)
        }
        menuAction.actions[key] = content
    }

    DisposableEffect(Unit) {
        onDispose {
            Log.d(TAG, "RegisterFabAction: re-setting fabActionManager")
            menuAction.actions.remove(key)
        }
    }
}
