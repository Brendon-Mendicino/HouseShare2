package lol.terabrendon.houseshare2.presentation.home

import androidx.compose.runtime.Composable
import lol.terabrendon.houseshare2.presentation.provider.LocalMenuActionManager

@Composable
fun AppBarActions() {
    val menuActionManager = LocalMenuActionManager.current

    menuActionManager.actions.values.forEach { action ->
        action.invoke()
    }

    // App Menu
    AppMenu()
}
