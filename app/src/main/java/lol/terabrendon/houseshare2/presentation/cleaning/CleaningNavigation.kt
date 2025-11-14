package lol.terabrendon.houseshare2.presentation.cleaning

import androidx.compose.material3.Text
import androidx.navigation3.runtime.EntryProviderScope
import lol.terabrendon.houseshare2.presentation.navigation.HomepageNavigation
import lol.terabrendon.houseshare2.presentation.navigation.MainNavigation
import lol.terabrendon.houseshare2.presentation.util.TOP_LEVEL_TRANSITION

fun EntryProviderScope<MainNavigation>.cleaningNavigation() {
    entry<HomepageNavigation.Cleaning>(
        metadata = TOP_LEVEL_TRANSITION
    ) {
        CleaningScreen()
        Text(text = "Cleaning")
    }
}