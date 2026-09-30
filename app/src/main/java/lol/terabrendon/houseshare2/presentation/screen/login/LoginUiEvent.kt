package lol.terabrendon.houseshare2.presentation.screen.login

sealed class LoginUiEvent {
    data object LoginSuccessful : LoginUiEvent()
    data object LoginFailed : LoginUiEvent()
    data class OpenRegistration(val url: String) : LoginUiEvent()
}
