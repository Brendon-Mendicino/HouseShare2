package lol.terabrendon.houseshare2.presentation.user

import androidx.navigation3.runtime.EntryProviderScope
import lol.terabrendon.houseshare2.presentation.navigation.HomepageNavigation
import lol.terabrendon.houseshare2.presentation.navigation.MainNavigation
import lol.terabrendon.houseshare2.presentation.navigation.Navigator

fun EntryProviderScope<MainNavigation>.userNavigation(navigator: Navigator<MainNavigation>) {
    entry<HomepageNavigation.UserProfile> {
        UserProfileScreen()
    }
}