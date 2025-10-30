package lol.terabrendon.houseshare2.presentation.login

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import lol.terabrendon.houseshare2.presentation.navigation.LoginNavigation
import lol.terabrendon.houseshare2.presentation.navigation.MainNavigation

fun NavGraphBuilder.loginNavigation() {
    navigation<MainNavigation.Login>(startDestination = LoginNavigation.UserLogin) {
        composable<LoginNavigation.UserLogin> {
            UserLoginScreen()
        }
    }
}