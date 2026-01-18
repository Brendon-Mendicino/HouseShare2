package lol.terabrendon.houseshare2.presentation.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import lol.terabrendon.houseshare2.data.repository.UserDataRepository
import lol.terabrendon.houseshare2.data.repository.UserDataRepository.Update.SelectedGroupId
import lol.terabrendon.houseshare2.data.repository.UserRepository
import lol.terabrendon.houseshare2.domain.usecase.GetLoggedUserUseCase
import lol.terabrendon.houseshare2.domain.usecase.GetSelectedGroupUseCase
import lol.terabrendon.houseshare2.presentation.groups.GroupEvent
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class GroupsViewModel @Inject constructor(
    userRepository: UserRepository,
    getLoggedUser: GetLoggedUserUseCase,
    private val sharedPreferencesRepository: UserDataRepository,
    getSelectedGroup: GetSelectedGroupUseCase,
) : ViewModel() {
    sealed class UiEvent {
        data class GroupSelected(val groupId: Long?) : UiEvent()
    }

    private val eventChannel = Channel<UiEvent>()
    val uiEvent = eventChannel.receiveAsFlow()

    val selectedGroup = getSelectedGroup()
        .map { it?.info }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val groups = getLoggedUser()
        .flatMapLatest { loggedUser ->
            loggedUser
                ?.let { userRepository.findGroupsByUserId(loggedUser.id) }
                ?: flowOf(emptyList())
        }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000L),
            emptyList(),
        )

    fun onEvent(event: GroupEvent) {
        when (event) {
            is GroupEvent.GroupSelected -> viewModelScope.launch {
                val groupAlreadySelected = event.group.groupId == selectedGroup.value?.groupId
                val selectedGroupId = if (groupAlreadySelected) null
                else event.group.groupId

                Timber.i("onEvent: updating selectedGroupId=%s", selectedGroupId)
                sharedPreferencesRepository.update(SelectedGroupId(selectedGroupId))
                eventChannel.send(UiEvent.GroupSelected(selectedGroupId))
            }
        }
    }
}