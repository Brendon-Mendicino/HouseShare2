package lol.terabrendon.houseshare2.presentation.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import lol.terabrendon.houseshare2.domain.usecase.GetLoggedUserUseCase
import lol.terabrendon.houseshare2.domain.usecase.LogoutUseCase
import javax.inject.Inject

@HiltViewModel
class UserViewModel @Inject constructor(
    loggedUserUseCase: GetLoggedUserUseCase,
    private val logoutUseCase: LogoutUseCase,
) : ViewModel() {
    data class State(
        val logoutPending: Boolean = false,
        val loginError: Boolean = false,
    )

    val user = loggedUserUseCase()

    private val _state = MutableStateFlow(State())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            user.collect { user ->
                _state.update { it.copy(loginError = user == null) }
            }
        }
    }

    fun onLogout() {
        viewModelScope.launch {
            _state.update { it.copy(logoutPending = true) }
            logoutUseCase()
            _state.update { it.copy(logoutPending = false) }
        }
    }
}