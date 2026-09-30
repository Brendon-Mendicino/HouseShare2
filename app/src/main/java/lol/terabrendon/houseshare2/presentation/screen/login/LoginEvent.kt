package lol.terabrendon.houseshare2.presentation.screen.login

sealed class LoginEvent {
    data class UsernameChanged(val username: String) : LoginEvent()
    data class PasswordChanged(val password: String) : LoginEvent()
    data object Login : LoginEvent()
    data object Register : LoginEvent()
}
