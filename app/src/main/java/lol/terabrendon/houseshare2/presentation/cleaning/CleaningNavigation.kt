package lol.terabrendon.houseshare2.presentation.cleaning

import androidx.compose.material3.Text
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import lol.terabrendon.houseshare2.presentation.navigation.HomepageNavigation

fun NavGraphBuilder.cleaningNavigation() {
    composable<HomepageNavigation.Cleaning> {
        CleaningScreen()
        Text(text = "Cleaning")
    }
}