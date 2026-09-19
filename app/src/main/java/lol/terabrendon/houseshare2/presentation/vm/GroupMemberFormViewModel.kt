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
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import lol.terabrendon.houseshare2.data.repository.GroupRepository
import lol.terabrendon.houseshare2.domain.form.GroupMemberFormState
import lol.terabrendon.houseshare2.domain.form.GroupMemberFormStateValidator
import lol.terabrendon.houseshare2.domain.form.toValidator
import lol.terabrendon.houseshare2.domain.mapper.toModel
import lol.terabrendon.houseshare2.presentation.navigation.HomepageNavigation
import lol.terabrendon.houseshare2.presentation.screen.groups.form.GroupMemberFormEvent
import lol.terabrendon.houseshare2.presentation.screen.groups.form.GroupMemberFormUiEvent
import lol.terabrendon.houseshare2.presentation.util.SnackbarController
import lol.terabrendon.houseshare2.presentation.util.SnackbarEvent
import lol.terabrendon.houseshare2.presentation.util.toUiText
import timber.log.Timber

@HiltViewModel(assistedFactory = GroupMemberFormViewModel.Factory::class)
class GroupMemberFormViewModel @AssistedInject constructor(
    @Assisted
    private val route: HomepageNavigation.GroupMemberForm,
    private val groupRepository: GroupRepository,
) : ViewModel() {
    @AssistedFactory
    interface Factory {
        fun create(route: HomepageNavigation.GroupMemberForm): GroupMemberFormViewModel
    }

    companion object {
        @Composable
        fun create(route: HomepageNavigation.GroupMemberForm) =
            hiltViewModel<GroupMemberFormViewModel, Factory>(creationCallback = { factory ->
                factory.create(route)
            })
    }

    private val _uiEvent = Channel<GroupMemberFormUiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    private val _groupMemberFormState = MutableStateFlow(GroupMemberFormState().toValidator())
    val groupMemberFormState = _groupMemberFormState.asStateFlow()

    /**
     * Helper function.
     */
    private fun MutableStateFlow<GroupMemberFormStateValidator>.updateState(inner: GroupMemberFormStateValidator.Updater.() -> Unit) =
        this.update { it.update(inner) }

    fun onEvent(event: GroupMemberFormEvent) {
        Timber.d("onEvent: event=%s", event)

        when (event) {
            is GroupMemberFormEvent.FirstNameChanged -> _groupMemberFormState.updateState {
                firstName = event.firstName
            }

            is GroupMemberFormEvent.LastNameChanged -> _groupMemberFormState.updateState {
                lastName = event.lastName.takeIf { name -> name.isNotEmpty() }
            }

            is GroupMemberFormEvent.PictureChanged -> _groupMemberFormState.updateState {
                picture = event.picture.takeIf { url -> url.isNotEmpty() }
            }

            is GroupMemberFormEvent.Submit -> viewModelScope.launch { onSubmit() }
        }
    }

    private suspend fun onSubmit() {
        val formState = _groupMemberFormState.value

        val formError = formState.errors.firstOrNull()
        if (formError != null) {
            val (parameterName, error) = formError
            val message = error.toUiText(parameterName)

            SnackbarController.sendEvent(SnackbarEvent(message = message))
            return
        }

        val member = formState.toData().toModel(route.groupId)

        Timber.i(
            "onSubmit: adding new member \"%s\" to groupId=%d",
            member.firstName,
            route.groupId,
        )

        val (_, err) = groupRepository.addMember(member)
        if (err != null) {
            SnackbarController.sendError(err)
            return
        }

        _uiEvent.send(GroupMemberFormUiEvent.SubmitSuccess)
    }
}
