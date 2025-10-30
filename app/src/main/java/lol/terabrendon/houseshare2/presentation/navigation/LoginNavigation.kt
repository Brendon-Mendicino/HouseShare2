package lol.terabrendon.houseshare2.presentation.navigation

import kotlinx.serialization.Serializable

sealed class LoginNavigation : MainNavigation() {
    @Serializable
    data object UserLogin : LoginNavigation()
}