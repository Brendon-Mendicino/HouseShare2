package lol.terabrendon.houseshare2.presentation.provider

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import lol.terabrendon.houseshare2.presentation.navigation.MainNavigation

@Composable
fun LocalBackStackProvider(backStack: List<MainNavigation>, content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalBackStack provides backStack) {
        content()
    }
}