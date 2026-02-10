package lol.terabrendon.houseshare2.presentation.screen.login

sealed class LoginEvent {
    data object Login : LoginEvent()
}