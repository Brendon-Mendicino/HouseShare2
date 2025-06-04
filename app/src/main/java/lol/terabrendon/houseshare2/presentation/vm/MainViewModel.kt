package lol.terabrendon.houseshare2.presentation.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
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
        .userPreferencesFlow
        .map { MainNavigation.from(it.mainDestination) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), MainNavigation.Loading)

    fun setCurrentNavigation(destination: KClass<out MainNavigation>) {
        viewModelScope.launch {
            userPreferencesRepository.updateMainDestination(destination)
        }
    }
}