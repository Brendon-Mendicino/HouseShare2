package lol.terabrendon.houseshare2.presentation.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import lol.terabrendon.houseshare2.domain.usecase.GetLoggedUserUseCase
import javax.inject.Inject

@HiltViewModel
class UserViewModel @Inject constructor(
    loggedUserUseCase: GetLoggedUserUseCase,
) : ViewModel() {
    val user = loggedUserUseCase()

    val isError = user.map { it == null }.stateIn(viewModelScope, SharingStarted.Eagerly, false)
}