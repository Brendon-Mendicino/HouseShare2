package lol.terabrendon.houseshare2.presentation.vm

import androidx.compose.runtime.Composable
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import lol.terabrendon.houseshare2.data.repository.GroupRepository
import lol.terabrendon.houseshare2.data.repository.UserRepository
import lol.terabrendon.houseshare2.domain.form.GroupFormState
import lol.terabrendon.houseshare2.domain.form.GroupFormStateValidator
import lol.terabrendon.houseshare2.domain.form.toValidator
import lol.terabrendon.houseshare2.domain.mapper.toModel
import lol.terabrendon.houseshare2.domain.model.UserModel
import lol.terabrendon.houseshare2.domain.usecase.GetLoggedUserUseCase
import lol.terabrendon.houseshare2.presentation.navigation.HomepageNavigation
import lol.terabrendon.houseshare2.presentation.screen.groups.form.GroupFormEvent
import lol.terabrendon.houseshare2.presentation.screen.groups.form.GroupFormUiEvent
import lol.terabrendon.houseshare2.presentation.util.SnackbarController
import lol.terabrendon.houseshare2.presentation.util.SnackbarEvent
import lol.terabrendon.houseshare2.presentation.util.toUiText
import lol.terabrendon.houseshare2.util.CombinedStateFlow
import lol.terabrendon.houseshare2.util.mapState
import timber.log.Timber

@HiltViewModel(assistedFactory = GroupFormViewModel.Factory::class)
class GroupFormViewModel @AssistedInject constructor(
    @Assisted
    private val route: HomepageNavigation.GroupInfoForm,
    private val groupRepository: GroupRepository,
    getLoggedUserUseCase: GetLoggedUserUseCase,
    userRepository: UserRepository,
) : ViewModel() {
    @AssistedFactory
    interface Factory {
        fun create(route: HomepageNavigation.GroupInfoForm): GroupFormViewModel
    }

    companion object {
        @Composable
        fun create(route: HomepageNavigation.GroupInfoForm) =
            hiltViewModel<GroupFormViewModel, Factory>(creationCallback = { factory ->
                factory.create(route)
            })
    }

    private var _uiEvent = Channel<GroupFormUiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    private val loggedUser = getLoggedUserUseCase()

    // <UserId, UserModel>
    private var _selectedUsers = MutableStateFlow<Map<Long, UserModel>>(emptyMap())
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

            Timber.i(
                "CombinedStateFlow: updating _groupFormState.users because their sizes differ from the one the user! users.size=%d, state.users.size=%d",
                users.size,
                state.users.value.size
            )

            state.copy(users = state.users.copy(users.values.toList()))
        }

    val groupFormState = _groupFormState.asStateFlow()

    init {
        // Pre-fill group form state
        viewModelScope.launch {
            if (route.groupId == null) return@launch

            val group = groupRepository.findById(route.groupId).first() ?: return@launch
            _groupFormState.updateState {
                name = group.info.name
                description = group.info.description
                users = group.users
                imageUrl = group.info.imageUrl?.toString()
            }
        }
    }

    val users = userRepository
        .findAll()
        // Filter current logged-in user
        .combine(loggedUser) { users, loggedUser ->
            users.filter { it != loggedUser }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    /**
     * Helper function.
     */
    private fun MutableStateFlow<GroupFormStateValidator>.updateState(inner: GroupFormStateValidator.Updater.() -> Unit) =
        this.update { it.update(inner) }

    fun onEvent(event: GroupFormEvent) {
        Timber.d("onEvent: event=%s", event)

        when (event) {
            is GroupFormEvent.NameChanged -> {
                _groupFormState.updateState { name = event.name }
            }

            is GroupFormEvent.DescriptionChanged -> {
                _groupFormState.updateState {
                    description = event.description.takeIf { desc -> desc.isNotEmpty() }
                }
            }

            is GroupFormEvent.ImageUrlChanged -> _groupFormState.updateState {
                imageUrl = event.url
            }

            is GroupFormEvent.UserListClicked -> {
                _selectedUsers.update {
                    val userAlreadySelected = event.user.id in it
                    Timber.d("onEvent: userAlreadySelected=%s", userAlreadySelected)

                    if (userAlreadySelected)
                        it - event.user.id
                    else
                        it + Pair(event.user.id, event.user)
                }
            }

            is GroupFormEvent.UserSelectedClicked -> _selectedUsers.update { it - event.userId }

            is GroupFormEvent.Submit -> viewModelScope.launch { onSubmit() }
        }
    }

    private suspend fun onSubmit() {
        val formState = _groupFormState.value
        val loggedUser =
            loggedUser.first() ?: throw IllegalStateException("No current logged-in user!")

        val formError = formState.errors.firstOrNull()
        if (formError != null) {
            val (parameterName, error) = formError
            val message = error.toUiText(parameterName)

            SnackbarController.sendEvent(SnackbarEvent(message = message))
            return
        }

        // Add current logged user to list of users in the group
        val formData = formState.toData().let {
            it.copy(users = it.users + loggedUser)
        }
        val newGroup = formData.toModel()

        Timber.i(
            "onSubmit: Inserting a new group with name \"%s\" to the repository",
            newGroup.info.name
        )

        val (_, err) = if (route.groupId == null) {
            groupRepository.insert(newGroup)
        } else {
            val toUpdate = newGroup.copy(info = newGroup.info.copy(groupId = route.groupId))
            groupRepository.update(toUpdate)
        }
        if (err != null) {
            SnackbarController.sendError(err)
            return
        }

        _uiEvent.send(GroupFormUiEvent.SubmitSuccess)
    }
}