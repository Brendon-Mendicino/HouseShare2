package lol.terabrendon.houseshare2.presentation.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import lol.terabrendon.houseshare2.presentation.navigation.MainNavigation
import lol.terabrendon.houseshare2.presentation.provider.RegisterTopBarConfig
import lol.terabrendon.houseshare2.presentation.provider.TopBarConfig
import kotlin.reflect.KClass

@Composable
fun RegisterBackNavIcon(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    route: KClass<out MainNavigation>,
) {
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
        route = route,
    )
}