package lol.terabrendon.houseshare2.presentation.vm

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.michaelbull.result.onFailure
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import lol.terabrendon.houseshare2.domain.usecase.GetLoggedUserUseCase
import lol.terabrendon.houseshare2.domain.usecase.StartLoginUseCase
import lol.terabrendon.houseshare2.presentation.login.LoginEvent
import lol.terabrendon.houseshare2.presentation.login.LoginUiEvent
import lol.terabrendon.houseshare2.presentation.util.SnackbarController
import lol.terabrendon.houseshare2.presentation.util.SnackbarEvent
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val getLoggedUser: GetLoggedUserUseCase,
    private val userLoginUseCase: StartLoginUseCase,
) : ViewModel() {
    companion object {
        private const val TAG = "LoginViewModel"
    }

    private var _uiEvent = Channel<LoginUiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    init {
        // When a user is found it means that login was performed correctly.
        viewModelScope.launch {
            getLoggedUser
                .execute()
                .first { it != null }

            Log.i(TAG, "init: user has logged in.")

            _uiEvent.send(LoginUiEvent.LoginSuccessful)
        }
    }

    fun onEvent(event: LoginEvent) {
        when (event) {
            LoginEvent.Login -> viewModelScope.launch {
                userLoginUseCase()
                    .onFailure {
                        Log.e(TAG, "onEvent: failed to perform login!", it)
                        SnackbarController.sendEvent(SnackbarEvent("Failed to login."))
                        _uiEvent.send(LoginUiEvent.LoginFailed)
                    }
            }
        }
    }
}