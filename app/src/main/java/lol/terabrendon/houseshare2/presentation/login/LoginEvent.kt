package lol.terabrendon.houseshare2.presentation.login

sealed class LoginEvent {
    data object Login : LoginEvent()
}