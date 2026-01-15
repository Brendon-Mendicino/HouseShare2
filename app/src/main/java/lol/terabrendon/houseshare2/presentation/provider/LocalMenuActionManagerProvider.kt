package lol.terabrendon.houseshare2.presentation.provider

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import timber.log.Timber

@Composable
fun LocalMenuActionManagerProvider(content: @Composable () -> Unit) {
    val menuAction = remember { MenuActionManager() }

    CompositionLocalProvider(LocalMenuActionManager provides menuAction) {
        content()
    }
}

@Composable
fun RegisterMenuAction(key: String, content: @Composable () -> Unit) {
    val menuAction = LocalMenuActionManager.current

    LaunchedEffect(Unit) {
        Timber.i("RegisterFabAction: setting-up fabActionManager")
        if (menuAction.actions.contains(key)) {
            val msg = "RegisterMenuAction: the same key=$key was registered more than once!"
            Timber.e(msg)
            throw IllegalStateException(msg)
        }
        menuAction.actions[key] = content
    }

    DisposableEffect(Unit) {
        onDispose {
            Timber.d("RegisterFabAction: re-setting fabActionManager")
            menuAction.actions.remove(key)
        }
    }
}
