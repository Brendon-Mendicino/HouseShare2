package lol.terabrendon.houseshare2.presentation.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import lol.terabrendon.houseshare2.data.repository.UserDataRepository
import lol.terabrendon.houseshare2.data.repository.UserDataRepository.Update.SendAnalytics
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userDataRepository: UserDataRepository,
) : ViewModel() {

    val sendAnalytics = userDataRepository
        .sendAnalytics
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), false)

    fun toggleAnalytics() = viewModelScope.launch {
        userDataRepository.update(SendAnalytics(sendAnalytics.value.not()))
    }
}