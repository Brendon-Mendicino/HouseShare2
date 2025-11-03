package lol.terabrendon.houseshare2.presentation.login

import android.util.Log
import androidx.navigation3.runtime.EntryProviderScope
import lol.terabrendon.houseshare2.presentation.navigation.HomepageNavigation
import lol.terabrendon.houseshare2.presentation.navigation.MainNavigation
import lol.terabrendon.houseshare2.presentation.navigation.Navigator

private const val TAG = "LoginNavigation"

fun EntryProviderScope<MainNavigation>.loginNavigation(navigator: Navigator<MainNavigation>) {
    entry<MainNavigation.Login> {
        UserLoginScreen(onFinish = {
            Log.i(TAG, "loginNavigation: login completed!")
            navigator.navigate(HomepageNavigation.Groups)
        })
    }
}