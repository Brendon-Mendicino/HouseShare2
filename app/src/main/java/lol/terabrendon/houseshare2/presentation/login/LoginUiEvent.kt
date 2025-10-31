package lol.terabrendon.houseshare2.presentation.login

sealed class LoginUiEvent {
    data object LoginSuccessful : LoginUiEvent()
    data object LoginFailed : LoginUiEvent()
}