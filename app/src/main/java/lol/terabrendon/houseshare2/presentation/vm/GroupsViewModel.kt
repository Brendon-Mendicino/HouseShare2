package lol.terabrendon.houseshare2.presentation.vm

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import lol.terabrendon.houseshare2.data.repository.UserPreferencesRepository
import lol.terabrendon.houseshare2.data.repository.UserRepository
import lol.terabrendon.houseshare2.domain.usecase.GetSelectedGroupUseCase
import lol.terabrendon.houseshare2.presentation.groups.GroupEvent
import lol.terabrendon.houseshare2.presentation.navigation.MainNavigation
import javax.inject.Inject

@HiltViewModel
class GroupsViewModel @Inject constructor(
    userRepository: UserRepository,
    private val sharedPreferencesRepository: UserPreferencesRepository,
    getSelectedGroup: GetSelectedGroupUseCase,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    companion object {
        private const val TAG: String = "GroupsViewModel"
    }

    private val args = savedStateHandle.toRoute<MainNavigation.Groups>()

    val selectedGroup = getSelectedGroup.execute()
        .map { it?.info }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), null)

    val groups = userRepository.findGroupsByUserId(args.currentUserId).stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000L), emptyList(),
    )

    fun onEvent(event: GroupEvent) {
        when (event) {
            is GroupEvent.GroupSelected -> viewModelScope.launch {
                val groupAlreadySelected = event.group.groupId == selectedGroup.value?.groupId
                val selectedGroupId = if (groupAlreadySelected) null
                else event.group.groupId

                Log.i(TAG, "onEvent: updating selectedGroupId=$selectedGroupId")
                sharedPreferencesRepository.updateSelectedGroupId(selectedGroupId)
            }
        }
    }
}