package lol.terabrendon.houseshare2.presentation.vm

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import lol.terabrendon.houseshare2.data.repository.UserPreferencesRepository
import lol.terabrendon.houseshare2.domain.usecase.StartingDestinationUseCase
import lol.terabrendon.houseshare2.presentation.navigation.MainNavigation
import javax.inject.Inject
import kotlin.reflect.KClass

@HiltViewModel
class MainViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val startingDestinationUseCase: StartingDestinationUseCase,
) : ViewModel() {
    companion object {
        private const val TAG = "MainViewModel"
    }

    // TODO: fix the whole navigation management with login
    val currentNavigation = userPreferencesRepository
        .savedDestination
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), MainNavigation.Loading)

    private val _startingDestination = MutableStateFlow<MainNavigation>(MainNavigation.Loading)
    val startingDestination = _startingDestination.asStateFlow()

    init {
        viewModelScope.launch {
            _startingDestination.value = startingDestinationUseCase()
        }
    }

    val topLevelRoutes = userPreferencesRepository
        .topLevelRoutes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    fun setCurrentNavigation(destination: KClass<out MainNavigation>) {
        viewModelScope.launch {
            Log.i(TAG, "setCurrentNavigation: new destination: $destination")
            userPreferencesRepository.updateMainDestination(destination)
        }
    }
}