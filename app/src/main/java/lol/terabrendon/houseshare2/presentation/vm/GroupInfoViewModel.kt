package lol.terabrendon.houseshare2.presentation.vm

import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.core.net.toUri
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.michaelbull.result.getOrElse
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import lol.terabrendon.houseshare2.BuildConfig
import lol.terabrendon.houseshare2.data.remote.api.GroupApi
import lol.terabrendon.houseshare2.data.repository.GroupRepository
import lol.terabrendon.houseshare2.domain.usecase.GetLoggedUserUseCase
import lol.terabrendon.houseshare2.presentation.navigation.HomepageNavigation
import lol.terabrendon.houseshare2.presentation.screen.groups.GroupInfoEvent
import lol.terabrendon.houseshare2.presentation.util.ActivityQueue
import lol.terabrendon.houseshare2.presentation.util.SnackbarController
import timber.log.Timber

@HiltViewModel(assistedFactory = GroupInfoViewModel.Factory::class)
class GroupInfoViewModel @AssistedInject constructor(
    @Assisted
    private val route: HomepageNavigation.GroupInfo,
    groupRepository: GroupRepository,
    private val groupApi: GroupApi,
    getLoggedUserUseCase: GetLoggedUserUseCase,
) : ViewModel() {
    @AssistedFactory
    interface Factory {
        fun create(route: HomepageNavigation.GroupInfo): GroupInfoViewModel
    }

    companion object {
        @Composable
        fun create(route: HomepageNavigation.GroupInfo) =
            hiltViewModel<GroupInfoViewModel, Factory>(creationCallback = { factory ->
                factory.create(route)
            })
    }

    sealed class UiEvent {
        data object NoGroupFound : UiEvent()
    }

    private val uiChannel = Channel<UiEvent>()
    val uiEvent = uiChannel.receiveAsFlow()

    val groupInfo = groupRepository
        .findById(route.groupId)
        .onEach { if (it == null) uiChannel.send(UiEvent.NoGroupFound) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), null)

    private val _inviteUrlLoading = MutableStateFlow(false)
    val inviteUrlLoading = _inviteUrlLoading.asStateFlow()


    val currentUser =
        getLoggedUserUseCase().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), null)

    fun onEvent(event: GroupInfoEvent): Unit = viewModelScope.launch {
        val group = groupInfo.value ?: return@launch

        when (event) {
            is GroupInfoEvent.ShareGroup -> {
                _inviteUrlLoading.value = true

                val res = groupApi.inviteUrl(group.info.groupId)
                val invite = res.getOrElse { err ->
                    SnackbarController.sendError(err)
                    return@launch
                }
                val inviteUri = invite.inviteUri.toUri()
                val base = BuildConfig.BASE_URL.toUri()

                val uri = base.buildUpon()
                    .appendEncodedPath(inviteUri.encodedPath?.removePrefix("/"))
                    .encodedQuery(inviteUri.encodedQuery)
                    .build()

                val intent = Intent().apply {
                    action = Intent.ACTION_SEND

                    putExtra(Intent.EXTRA_TEXT, uri.toString())
                    putExtra(Intent.EXTRA_TITLE, "Group invitation link")

                    type = "text/uri-list"
                    flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                }

                val share = Intent.createChooser(intent, null)

                Timber.i("onEvent: sending group invite url")
                ActivityQueue.sendIntent(share)

                _inviteUrlLoading.value = false
            }
        }
    }.let { }
}
