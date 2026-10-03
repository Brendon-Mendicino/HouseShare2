package lol.terabrendon.houseshare2.presentation.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import lol.terabrendon.houseshare2.BuildConfig
import lol.terabrendon.houseshare2.R
import lol.terabrendon.houseshare2.data.local.preferences.UserData
import lol.terabrendon.houseshare2.data.remote.DebugServerUrl
import lol.terabrendon.houseshare2.data.repository.UserDataRepository
import lol.terabrendon.houseshare2.data.repository.UserDataRepository.Update.AppTheme
import lol.terabrendon.houseshare2.data.repository.UserDataRepository.Update.DynamicColors
import lol.terabrendon.houseshare2.data.repository.UserDataRepository.Update.SendAnalytics
import lol.terabrendon.houseshare2.domain.usecase.FinishLogoutUseCase
import lol.terabrendon.houseshare2.presentation.util.SnackbarController
import timber.log.Timber
import javax.inject.Inject

data class SettingsState(
    val sendAnalytics: Boolean = false,
    val appTheme: UserData.Theme = UserData.Theme.System,
    val dynamicColors: Boolean = false,
    val showServerUrl: Boolean = false,
    val serverUrl: String = DebugServerUrl.default.toString(),
)

sealed interface SettingsEvent {
    data object AnalyticsToggled : SettingsEvent
    data class ThemeChanged(val appTheme: UserData.Theme) : SettingsEvent
    data object DynamicToggled : SettingsEvent

    /**
     * Debug builds only, `null` restores the default server.
     */
    data class ServerUrlChanged(val url: String?) : SettingsEvent
}

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userDataRepository: UserDataRepository,
    private val finishLogoutUseCase: FinishLogoutUseCase,
) : ViewModel() {

    val uiState = combine(userDataRepository.data, DebugServerUrl.current) { data, serverUrl ->
        SettingsState(
            sendAnalytics = data.sendAnalytics,
            appTheme = data.appTheme,
            dynamicColors = data.dynamicColors,
            showServerUrl = BuildConfig.DEBUG,
            serverUrl = serverUrl.toString(),
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
                is SettingsEvent.ServerUrlChanged -> changeServerUrl(event.url)
            }
        }
    }

    private suspend fun changeServerUrl(input: String?) {
        if (!BuildConfig.DEBUG) return

        val url = if (input == null) null else DebugServerUrl.parse(input) ?: run {
            SnackbarController.sendRes(R.string.invalid_server_url)
            return
        }

        if ((url ?: DebugServerUrl.default) == DebugServerUrl.current.value) return

        Timber.i("changeServerUrl: switching server to %s", url ?: DebugServerUrl.default)
        DebugServerUrl.set(url)

        // The session, the cookies and the stored data all belong to the previous server.
        finishLogoutUseCase()
        SnackbarController.sendRes(R.string.server_url_changed)
    }
}
