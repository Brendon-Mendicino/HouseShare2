package lol.terabrendon.houseshare2.presentation.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import lol.terabrendon.houseshare2.data.repository.UserPreferencesRepository
import lol.terabrendon.houseshare2.presentation.navigation.MainNavigation
import javax.inject.Inject
import kotlin.reflect.KClass

@HiltViewModel
class MainViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {
    companion object {
        private const val TAG = "MainViewModel"
    }


    val currentNavigation = userPreferencesRepository
        .savedDestination
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), MainNavigation.Loading)

    val startingDestination = userPreferencesRepository
        .savedDestination
        .take(1)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), MainNavigation.Loading)

    val topLevelRoutes = userPreferencesRepository
        .topLevelRoutes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    fun setCurrentNavigation(destination: KClass<out MainNavigation>) {
        viewModelScope.launch {
            userPreferencesRepository.updateMainDestination(destination)
        }
    }
}