package lol.terabrendon.houseshare2.presentation.vm

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import lol.terabrendon.houseshare2.data.repository.UserPreferencesRepository
import lol.terabrendon.houseshare2.data.repository.UserRepository
import lol.terabrendon.houseshare2.domain.usecase.GetLoggedUserUseCase
import lol.terabrendon.houseshare2.domain.usecase.GetSelectedGroupUseCase
import lol.terabrendon.houseshare2.presentation.groups.GroupEvent
import javax.inject.Inject

@HiltViewModel
class GroupsViewModel @Inject constructor(
    userRepository: UserRepository,
    getLoggedUser: GetLoggedUserUseCase,
    private val sharedPreferencesRepository: UserPreferencesRepository,
    getSelectedGroup: GetSelectedGroupUseCase,
) : ViewModel() {
    companion object {
        private const val TAG: String = "GroupsViewModel"
    }

    val selectedGroup = getSelectedGroup.execute()
        .map { it?.info }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val groups = getLoggedUser
        .execute()
        .flatMapLatest { loggedUser ->
            loggedUser
                ?.let { userRepository.findGroupsByUserId(loggedUser.id) }
                ?: flowOf(emptyList())
        }
        .stateIn(
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