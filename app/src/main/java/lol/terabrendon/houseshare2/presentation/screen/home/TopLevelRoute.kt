package lol.terabrendon.houseshare2.presentation.screen.home

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.vector.ImageVector
import lol.terabrendon.houseshare2.presentation.navigation.MainNavigation

data class TopLevelRoute(
    @StringRes
    val name: Int,
    val route: MainNavigation,
    val icon: ImageVector,
)