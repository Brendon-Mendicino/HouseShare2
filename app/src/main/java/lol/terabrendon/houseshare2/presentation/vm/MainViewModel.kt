package lol.terabrendon.houseshare2.presentation.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
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

    val theme = userDataRepository
        .theme
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), UserData.Theme.System)

    val dynamicColor = userDataRepository
        .dynamicColor
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), false)
}