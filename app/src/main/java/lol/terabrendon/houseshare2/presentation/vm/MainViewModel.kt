package lol.terabrendon.houseshare2.presentation.vm

import android.content.Context
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import lol.terabrendon.houseshare2.presentation.navigation.MainDestination
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    @ApplicationContext private val applicationContext: Context,
) : ViewModel() {


    private var _previousDestination = MutableStateFlow<MainDestination?>(null)
    val previousDestination: StateFlow<MainDestination?> = _previousDestination


    private var _currentDestination = MutableStateFlow(MainDestination.Cleaning)
    val currentDestination: StateFlow<MainDestination> = _currentDestination


    fun setCurrentDestination(destination: MainDestination) {
        _previousDestination.update { currentDestination.value }
        _currentDestination.update { destination }
    }
}