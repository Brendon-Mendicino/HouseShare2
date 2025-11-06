package lol.terabrendon.houseshare2.presentation.vm

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import lol.terabrendon.houseshare2.domain.usecase.GetLoggedUserUseCase
import javax.inject.Inject

@HiltViewModel
class UserViewModel @Inject constructor(
    private val loggedUserUseCase: GetLoggedUserUseCase,
) : ViewModel() {

}