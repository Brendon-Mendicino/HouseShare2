package lol.terabrendon.houseshare2.presentation.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import lol.terabrendon.houseshare2.data.repository.UserRepository
import lol.terabrendon.houseshare2.domain.model.UserModel
import javax.inject.Inject

@HiltViewModel
class AppMenuViewModel @Inject constructor(
    private val userRepository: UserRepository,
) : ViewModel() {


    fun onInsertUser(user: UserModel) {
        viewModelScope.launch {
            userRepository.insert(user)
        }
    }
}