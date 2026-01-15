package lol.terabrendon.houseshare2.presentation.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.michaelbull.result.onFailure
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import lol.terabrendon.houseshare2.R
import lol.terabrendon.houseshare2.domain.usecase.GetLoggedUserUseCase
import lol.terabrendon.houseshare2.domain.usecase.StartLoginUseCase
import lol.terabrendon.houseshare2.presentation.login.LoginEvent
import lol.terabrendon.houseshare2.presentation.login.LoginUiEvent
import lol.terabrendon.houseshare2.presentation.util.SnackbarController
import lol.terabrendon.houseshare2.presentation.util.SnackbarEvent
import lol.terabrendon.houseshare2.presentation.util.UiText
import lol.terabrendon.houseshare2.presentation.util.toUiText
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val getLoggedUser: GetLoggedUserUseCase,
    private val userLoginUseCase: StartLoginUseCase,
) : ViewModel() {
    private var _uiEvent = Channel<LoginUiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

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

    fun onEvent(event: LoginEvent) {
        when (event) {
            LoginEvent.Login -> viewModelScope.launch {
                userLoginUseCase()
                    .onFailure { error ->
                        Timber.w("onEvent: failed to perform login! response=%s", error)

                        SnackbarController.sendEvent(
                            SnackbarEvent(
                                message = UiText.Res(R.string.login_failed) + error.toUiText()
                            )
                        )
                        _uiEvent.send(LoginUiEvent.LoginFailed)
                    }
            }
        }
    }
}