package lol.terabrendon.houseshare2.presentation.vm

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import lol.terabrendon.houseshare2.data.repository.GroupRepository
import lol.terabrendon.houseshare2.data.repository.UserRepository
import lol.terabrendon.houseshare2.domain.mapper.GroupFormStateMapper
import lol.terabrendon.houseshare2.domain.model.GroupFormState
import lol.terabrendon.houseshare2.domain.model.UserModel
import lol.terabrendon.houseshare2.domain.model.toValidator
import lol.terabrendon.houseshare2.presentation.groups.form.GroupFormEvent
import lol.terabrendon.houseshare2.presentation.groups.form.GroupFormUiEvent
import lol.terabrendon.houseshare2.util.CombinedStateFlow
import lol.terabrendon.houseshare2.util.mapState
import javax.inject.Inject

@HiltViewModel
class GroupFormViewModel @Inject constructor(
    private val groupRepository: GroupRepository,
    userRepository: UserRepository,
) : ViewModel() {
    companion object {
        const val TAG: String = "GroupFormViewModel"
    }

    private val groupFormStateMapper = GroupFormStateMapper()

    private var _uiEvent = Channel<GroupFormUiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    // TODO: the current logged in user should be in the list by default
    private var _selectedUsers = MutableStateFlow<MutableMap<Long, UserModel>>(linkedMapOf())
    val selectedUsers = _selectedUsers.mapState(viewModelScope) { it.keys.toSet() }

    private var _groupFormState =
        CombinedStateFlow(
            GroupFormState().toValidator(),
            viewModelScope,
            _selectedUsers,
        ) { state, users ->
            val stateUsersSize = state.users.value.size

            if (stateUsersSize == users.size)
                return@CombinedStateFlow state

            Log.i(
                TAG,
                "CombinedStateFlow: updating _groupFormState.users because their sizes differ from the one the user! users.size=${users.size}, state.users.size=${state.users.value.size}"
            )

            state.copy(users = state.users.copy(users.values.toList()))
        }

    val groupFormState = _groupFormState.asStateFlow()

    val users = userRepository.findAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    fun onEvent(event: GroupFormEvent) {
        Log.d(TAG, "onEvent: event=$event")

        when (event) {
            is GroupFormEvent.NameChanged -> {
                _groupFormState.update {
                    it.copy(name = it.name.update(event.name))
                }
            }

            is GroupFormEvent.DescriptionChanged -> {
                _groupFormState.update {
                    it.copy(description = it.description.update(event.description))
                }
            }

            is GroupFormEvent.UserListClicked -> {
                _selectedUsers.update {
                    val userAlreadySelected = event.user.id in it
                    Log.d(TAG, "onEvent: userAlreadySelected=$userAlreadySelected")

                    it.toMutableMap().apply {
                        if (userAlreadySelected) remove(event.user.id)
                        else set(event.user.id, event.user)
                    }
                }
            }

            is GroupFormEvent.UserSelectedClicked -> {
                _selectedUsers.update {
                    it.toMutableMap().apply { remove(event.userId) }
                }
            }

            is GroupFormEvent.Submit -> onSubmit()
        }
    }

    private fun onSubmit() {
        val formState = _groupFormState.value

        if (formState.isError) {
            viewModelScope.launch {
                _uiEvent.send(GroupFormUiEvent.SubmitFailure(formState.errors.first()))
            }
            return
        }

        val newGroup = groupFormStateMapper.map(formState.toData())

        viewModelScope.launch {
            Log.i(
                TAG,
                "onSubmit: Inserting a new group with name \"${newGroup.info.name}\" to the repository"
            )

            groupRepository.insert(newGroup)

            _uiEvent.send(GroupFormUiEvent.SubmitSuccess)
        }
    }
}