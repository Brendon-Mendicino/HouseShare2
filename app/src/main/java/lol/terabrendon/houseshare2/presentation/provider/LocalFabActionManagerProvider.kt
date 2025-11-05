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

private const val TAG: String = "LocalFabActionManagerProvider"

/**
 * Provides the [LocalFabActionManager] to all the children inside [content].
 *
 * All children can register an action on the fab using [RegisterFabAction].
 *
 * This makes very easy for a screen to implement the `onClick()` for the FAB.
 *
 * # Examples:
 *
 * ```
 * @Composable
 * fun GroupInfoFormScreen(
 *     viewModel: ViewModel,
 * ) {
 *     RegisterFabAction {
 *         Log.d(TAG, "GroupInfoFormScreen: fab has been clicked")
 *         viewModel.onEvent(GroupFormEvent.Submit)
 *     }
 * }
 * ```
 */
@Composable
fun LocalFabActionManagerProvider(content: @Composable ((FabActionManager) -> Unit)) {
    val fabActionManager = remember { FabActionManager() }

    CompositionLocalProvider(LocalFabActionManager provides fabActionManager) {
        content(fabActionManager)
    }
}

@Composable
fun RegisterFabAction(enabled: Boolean = true, action: () -> Unit) {
    val fabActionManager = LocalFabActionManager.current

    // Use the backStack to make dispositions happen quicker
    val backStack = LocalBackStack.current
    var backStackChanged by remember { mutableIntStateOf(0) }

    if (backStack != null) {
        LaunchedEffect(backStack) {
            backStackChanged += 1
        }
    }

    DisposableEffect(action, enabled, backStackChanged) {
        if (!enabled || backStackChanged > 1)
            return@DisposableEffect onDispose { }

        Log.i(TAG, "RegisterFabAction: setting-up fabActionManager")
        fabActionManager.setState(action)

        onDispose {
            fabActionManager.resetState()
            Log.d(TAG, "RegisterFabAction: re-setting fabActionManager")
        }
    }
}