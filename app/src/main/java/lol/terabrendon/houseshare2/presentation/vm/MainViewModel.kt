package lol.terabrendon.houseshare2.presentation.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import lol.terabrendon.houseshare2.data.local.preferences.UserData
import lol.terabrendon.houseshare2.data.repository.AuthRepository
import lol.terabrendon.houseshare2.data.repository.UserDataRepository
import lol.terabrendon.houseshare2.data.util.NetworkMonitor
import lol.terabrendon.houseshare2.presentation.navigation.MainNavigation
import lol.terabrendon.houseshare2.presentation.navigation.Navigator
import lol.terabrendon.houseshare2.presentation.navigation.NavigatorImpl
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    userDataRepository: UserDataRepository,
    authRepository: AuthRepository,
    networkMonitor: NetworkMonitor,
) : ViewModel() {
    init {
        Timber.i("init: initialized MainViewModel")
    }

    val navigator: Navigator<MainNavigation> =
        NavigatorImpl(userDataRepository, viewModelScope, authRepository, networkMonitor)

    val appTheme = userDataRepository.data.map { it.appTheme }.distinctUntilChanged()
        .stateIn(viewModelScope, SharingStarted.Eagerly, UserData.Theme.System)

    val dynamicColors = userDataRepository.data.map { it.dynamicColors }.distinctUntilChanged()
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)
}