package lol.terabrendon.houseshare2.presentation.settings

import androidx.navigation3.runtime.EntryProviderScope
import lol.terabrendon.houseshare2.presentation.navigation.HomepageNavigation
import lol.terabrendon.houseshare2.presentation.navigation.MainNavigation
import lol.terabrendon.houseshare2.presentation.navigation.Navigator

fun EntryProviderScope<MainNavigation>.settingsNavigation(navigator: Navigator<MainNavigation>) {
    entry<HomepageNavigation.Settings> {
        SettingsScreen()
    }
}