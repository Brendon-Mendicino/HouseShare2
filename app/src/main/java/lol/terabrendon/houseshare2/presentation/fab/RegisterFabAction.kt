package lol.terabrendon.houseshare2.presentation.fab

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import lol.terabrendon.houseshare2.presentation.util.LocalFabActionManager

private const val TAG: String = "RegisterFabAction"

@Composable
fun RegisterFabAction(action: () -> Unit) {
    val fabActionManager = LocalFabActionManager.current

    LaunchedEffect(Unit) {
        Log.i(TAG, "GroupInfoFormScreen: setting-up fabActionManager")
        fabActionManager.setFabAction {
            action()
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            Log.d(TAG, "GroupInfoFormScreen: re-setting fabActionManager")
            fabActionManager.setFabAction(null)
        }
    }
}
