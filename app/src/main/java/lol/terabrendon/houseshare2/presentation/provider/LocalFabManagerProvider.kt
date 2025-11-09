package lol.terabrendon.houseshare2.presentation.provider

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import lol.terabrendon.houseshare2.presentation.navigation.MainNavigation

const val TAG_FAB: String = "LocalFabActionManagerProvider"

/**
 * Provides the [LocalFabManager] to all the children inside [content].
 *
 * All children can register an action on the fab using [RegisterFabConfig].
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
 *     RegisterFabConfig(...)
 * }
 * ```
 */
@Composable
fun LocalFabManagerProvider(content: @Composable (() -> Unit)) {
    val fabManager = remember { FabManager() }

    CompositionLocalProvider(LocalFabManager provides fabManager) {
        content()
    }
}

@Composable
inline fun <reified T : MainNavigation> RegisterFabConfig(
    config: FabConfig,
    enabled: Boolean = true,
) {
    val fabManager = LocalFabManager.current

    // Use the backStack to make dispositions happen quicker
    val backStack = LocalBackStack.current
    val enabled = enabled && backStack?.lastOrNull() is T

    DisposableEffect(config, enabled) {
        if (!enabled)
            return@DisposableEffect onDispose { }

        Log.i(TAG_FAB, "RegisterFabConfig: setting-up fabManager")
        // This is needed because of the android:enableOnBackInvokedCallback="true"
        val key = fabManager.putState(config)

        onDispose {
            fabManager.removeState(key)
            Log.d(TAG_FAB, "RegisterFabConfig: re-setting fabManager")
        }
    }
}