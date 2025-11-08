package lol.terabrendon.houseshare2.presentation.vm

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import lol.terabrendon.houseshare2.data.repository.AuthRepository
import lol.terabrendon.houseshare2.data.repository.UserDataRepository
import lol.terabrendon.houseshare2.presentation.navigation.MainNavigation
import lol.terabrendon.houseshare2.presentation.navigation.Navigator
import lol.terabrendon.houseshare2.presentation.navigation.NavigatorImpl
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    userDataRepository: UserDataRepository,
    authRepository: AuthRepository,
) : ViewModel() {
    companion object {
        private const val TAG = "MainViewModel"
    }

    init {
        Log.i(TAG, "init: initialized MainViewModel")
    }

    val navigator: Navigator<MainNavigation> =
        NavigatorImpl(userDataRepository, viewModelScope, authRepository)
}