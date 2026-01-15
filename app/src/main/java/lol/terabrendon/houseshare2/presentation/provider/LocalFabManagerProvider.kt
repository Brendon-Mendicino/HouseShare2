package lol.terabrendon.houseshare2.presentation.provider

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import lol.terabrendon.houseshare2.presentation.navigation.MainNavigation
import timber.log.Timber

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

    // Add the current route to the configuration in case the caller did not insert it
    // This is likely the case, being a very repetitive action, this can be done
    // by the register.
    val config =
        if (config.route == null && backStack != null) config.withRoute(backStack.last()) else config

    DisposableEffect(config, enabled) {
        if (!enabled) return@DisposableEffect onDispose { }

        Timber.d("RegisterFabConfig: setting-up fabManager")
        // This is needed because of the android:enableOnBackInvokedCallback="true"
        val key = fabManager.putState(config)

        onDispose {
            fabManager.removeState(key)
            Timber.d("RegisterFabConfig: re-setting fabManager")
        }
    }
}