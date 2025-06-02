package lol.terabrendon.houseshare2.presentation.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import lol.terabrendon.houseshare2.data.repository.UserPreferencesRepository
import lol.terabrendon.houseshare2.presentation.navigation.MainDestination
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {
    companion object {
        private const val TAG = "MainViewModel"
    }


    val currentDestination = userPreferencesRepository
        .userPreferencesFlow
        .map { MainDestination.from(it.mainDestination) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), MainDestination.Loading)

    fun setCurrentDestination(destination: MainDestination) {
        viewModelScope.launch {
            userPreferencesRepository.updateMainDestination(destination)
        }
    }
}