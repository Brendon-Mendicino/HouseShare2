package lol.terabrendon.houseshare2.presentation.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import lol.terabrendon.houseshare2.data.local.preferences.UserData
import lol.terabrendon.houseshare2.data.repository.UserDataRepository
import lol.terabrendon.houseshare2.data.repository.UserDataRepository.Update.AppTheme
import lol.terabrendon.houseshare2.data.repository.UserDataRepository.Update.DynamicColors
import lol.terabrendon.houseshare2.data.repository.UserDataRepository.Update.SendAnalytics
import javax.inject.Inject

data class SettingsState(
    val sendAnalytics: Boolean = false,
    val appTheme: UserData.Theme = UserData.Theme.System,
    val dynamicColors: Boolean = false,
)

sealed interface SettingsEvent {
    data object AnalyticsToggled : SettingsEvent
    data class ThemeChanged(val appTheme: UserData.Theme) : SettingsEvent
    data object DynamicToggled : SettingsEvent
}

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userDataRepository: UserDataRepository,
) : ViewModel() {

    val uiState = userDataRepository
        .data
        .map {
            SettingsState(
                sendAnalytics = it.sendAnalytics,
                appTheme = it.appTheme,
                dynamicColors = it.dynamicColors
            )
        }
        .distinctUntilChanged()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), SettingsState())

    fun onEvent(event: SettingsEvent) {
        viewModelScope.launch {
            when (event) {
                SettingsEvent.AnalyticsToggled -> userDataRepository.update(SendAnalytics(!uiState.value.sendAnalytics))
                SettingsEvent.DynamicToggled -> userDataRepository.update(DynamicColors(!uiState.value.dynamicColors))
                is SettingsEvent.ThemeChanged -> userDataRepository.update(AppTheme(event.appTheme))
            }
        }
    }
}