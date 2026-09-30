package lol.terabrendon.houseshare2.presentation.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.michaelbull.result.onFailure
import com.github.michaelbull.result.onSuccess
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.updateAndGet
import kotlinx.coroutines.launch
import lol.terabrendon.houseshare2.R
import lol.terabrendon.houseshare2.domain.form.LoginFormState
import lol.terabrendon.houseshare2.domain.form.LoginFormStateValidator
import lol.terabrendon.houseshare2.domain.form.toValidator
import lol.terabrendon.houseshare2.domain.form.touchAll
import lol.terabrendon.houseshare2.domain.usecase.GetLoggedUserUseCase
import lol.terabrendon.houseshare2.domain.usecase.GetRegistrationUrlUseCase
import lol.terabrendon.houseshare2.domain.usecase.LoginUseCase
import lol.terabrendon.houseshare2.presentation.screen.login.LoginEvent
import lol.terabrendon.houseshare2.presentation.screen.login.LoginUiEvent
import lol.terabrendon.houseshare2.presentation.util.SnackbarController
import lol.terabrendon.houseshare2.presentation.util.SnackbarEvent
import lol.terabrendon.houseshare2.presentation.util.UiText
import lol.terabrendon.houseshare2.presentation.util.errorUiText
import lol.terabrendon.houseshare2.presentation.util.toUiText
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val getLoggedUser: GetLoggedUserUseCase,
    private val loginUseCase: LoginUseCase,
    private val getRegistrationUrl: GetRegistrationUrlUseCase,
) : ViewModel() {
    private var _uiEvent = Channel<LoginUiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    private val _formState = MutableStateFlow(LoginFormState().toValidator())
    val formState = _formState.asStateFlow()

    private val _isPending = MutableStateFlow(false)
    val isPending = _isPending.asStateFlow()

    init {
        // When a user is found it means that login was performed correctly.
        viewModelScope.launch {
            getLoggedUser()
                .filterNotNull()
                .collect {
                    Timber.i("init: user has logged in.")

                    _uiEvent.send(LoginUiEvent.LoginSuccessful)

                    delay(5000L)
                }
        }
    }

    private fun MutableStateFlow<LoginFormStateValidator>.updateState(inner: LoginFormStateValidator.Updater.() -> Unit) =
        this.update { it.update(inner) }

    fun onEvent(event: LoginEvent) {
        when (event) {
            is LoginEvent.UsernameChanged -> _formState.updateState {
                username = event.username
            }

            is LoginEvent.PasswordChanged -> _formState.updateState {
                password = event.password
            }

            LoginEvent.Login -> viewModelScope.launch { onLogin() }

            LoginEvent.Register -> viewModelScope.launch { onRegister() }
        }
    }

    private suspend fun onRegister() {
        _isPending.update { true }

        getRegistrationUrl()
            .onSuccess { url -> _uiEvent.send(LoginUiEvent.OpenRegistration(url)) }
            .onFailure { err ->
                Timber.w("onRegister: failed to get the registration url! error=%s", err)

                SnackbarController.sendError(err)
            }

        _isPending.update { false }
    }

    private suspend fun onLogin() {
        val formState = _formState.updateAndGet { it.touchAll() }

        val formError = formState.errorUiText()
        if (formError != null) {
            SnackbarController.sendEvent(SnackbarEvent(message = formError))
            return
        }

        val data = formState.toData()

        _isPending.update { true }

        loginUseCase(username = data.username, password = data.password)
            .onFailure { err ->
                Timber.w("onLogin: failed to perform login! error=%s", err)

                SnackbarController.sendEvent(
                    SnackbarEvent(
                        message = UiText.Res(R.string.login_failed) + err.toUiText()
                    )
                )

                _uiEvent.send(LoginUiEvent.LoginFailed)
            }

        _isPending.update { false }
    }
}
