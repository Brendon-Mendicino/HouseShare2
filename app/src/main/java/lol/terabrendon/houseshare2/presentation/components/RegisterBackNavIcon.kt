package lol.terabrendon.houseshare2.presentation.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import lol.terabrendon.houseshare2.presentation.provider.RegisterTopBarConfig
import lol.terabrendon.houseshare2.presentation.provider.TopBarConfig

@Composable
fun RegisterBackNavIcon(onClick: () -> Unit, modifier: Modifier = Modifier) {
    RegisterTopBarConfig(
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