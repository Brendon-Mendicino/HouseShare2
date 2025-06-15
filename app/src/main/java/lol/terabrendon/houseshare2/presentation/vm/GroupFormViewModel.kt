package lol.terabrendon.houseshare2.presentation.vm

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
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
import lol.terabrendon.houseshare2.util.CombinedStateFlow
import javax.inject.Inject

@HiltViewModel
class GroupFormViewModel @Inject constructor(
    private val groupRepository: GroupRepository,
    private val userRepository: UserRepository,
) : ViewModel() {
    init {
        Log.d(TAG, "init: ")
    }

    companion object {
        const val TAG: String = "GroupFormViewModel"
    }

    private val groupFormStateMapper = GroupFormStateMapper()

    private var selectedUsers = MutableStateFlow<LinkedHashMap<Long, UserModel>>(linkedMapOf())

    private var _groupFormState =
        CombinedStateFlow(
            GroupFormState().toValidator(),
            viewModelScope,
            selectedUsers,
        ) { state, users ->
            val stateUsersSize = state.users.value.size

            if (stateUsersSize == users.size) {
                Log.d(TAG, "combinedstateflow: ${state}")
                return@CombinedStateFlow state
            }

            Log.d(TAG, "enteriiinggggg: ${state}")
//            state.copy(users = state.users.copy(users.values.toList()))
            state
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
                selectedUsers.update {
                    val userAlreadySelected = event.user.id in it

                    LinkedHashMap(
                        if (userAlreadySelected) it.apply { remove(event.user.id) }
                        else it.apply { set(event.user.id, event.user) }
                    )
                }
            }

            is GroupFormEvent.UserSelectedClicked -> {
                selectedUsers.update {
                    LinkedHashMap(it.apply { remove(event.userId) })
                }
            }

            is GroupFormEvent.Submit -> onSubmit()
        }
    }

    private fun onSubmit() {
        val newGroup = groupFormStateMapper.map(_groupFormState.value.toData())

        viewModelScope.launch {
            groupRepository.insert(newGroup)
        }
    }
}