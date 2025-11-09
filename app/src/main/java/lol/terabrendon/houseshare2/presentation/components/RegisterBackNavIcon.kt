package lol.terabrendon.houseshare2.presentation.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import lol.terabrendon.houseshare2.presentation.navigation.MainNavigation
import lol.terabrendon.houseshare2.presentation.provider.RegisterTopBarConfig
import lol.terabrendon.houseshare2.presentation.provider.TopBarConfig

@Composable
inline fun <reified T : MainNavigation> RegisterBackNavIcon(
    crossinline onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    RegisterTopBarConfig<T>(
        config = TopBarConfig(
            navigationIcon = {
                BackButtonIcon(
                    modifier = modifier,
                    onClick = {
                        onClick()
                    }
                )
            }
        ),
    )
}