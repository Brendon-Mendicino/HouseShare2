package lol.terabrendon.houseshare2.presentation.vm

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import lol.terabrendon.houseshare2.data.repository.UserRepository
import lol.terabrendon.houseshare2.presentation.navigation.MainNavigation
import javax.inject.Inject

@HiltViewModel
class GroupsViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val args = savedStateHandle.toRoute<MainNavigation.Groups>()

    val groups = userRepository.findGroupsByUserId(args.currentUserId).stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000L), emptyList(),
    )
}