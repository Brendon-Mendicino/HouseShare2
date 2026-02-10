package lol.terabrendon.houseshare2.presentation.screen.login

import androidx.navigation3.runtime.EntryProviderScope
import lol.terabrendon.houseshare2.presentation.navigation.HomepageNavigation
import lol.terabrendon.houseshare2.presentation.navigation.MainNavigation
import lol.terabrendon.houseshare2.presentation.navigation.Navigator
import timber.log.Timber

fun EntryProviderScope<MainNavigation>.loginNavigation(navigator: Navigator<MainNavigation>) {
    entry<MainNavigation.Login> {
        UserLoginScreen(onFinish = {
            Timber.i("loginNavigation: login completed!")
            navigator.navigate(HomepageNavigation.Groups)
        })
    }
}