package lol.terabrendon.houseshare2.presentation.vm

import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import lol.terabrendon.houseshare2.BuildConfig
import lol.terabrendon.houseshare2.data.remote.api.GroupApi
import lol.terabrendon.houseshare2.data.repository.GroupRepository
import lol.terabrendon.houseshare2.presentation.groups.GroupInfoEvent
import lol.terabrendon.houseshare2.presentation.navigation.HomepageNavigation
import lol.terabrendon.houseshare2.presentation.util.ActivityQueue

@HiltViewModel(assistedFactory = GroupInfoViewModel.Factory::class)
class GroupInfoViewModel @AssistedInject constructor(
    @Assisted
    private val route: HomepageNavigation.GroupInfo,
    private val groupRepository: GroupRepository,
    private val groupApi: GroupApi,
) : ViewModel() {
    companion object {
        private const val TAG = "GroupInfoViewModel"
    }

    @AssistedFactory
    interface Factory {
        fun create(route: HomepageNavigation.GroupInfo): GroupInfoViewModel
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

    fun onEvent(event: GroupInfoEvent): Unit = viewModelScope.launch {
        val group = groupInfo.value ?: return@launch

        when (event) {
            is GroupInfoEvent.ShareGroup -> {
                val invite = groupApi.inviteUrl(group.info.groupId).inviteUri.toUri()
                val base = BuildConfig.BASE_URL.toUri()

                val uri = Uri.Builder()
                    .scheme(base.scheme!!)
                    .encodedAuthority(base.encodedAuthority!!)
                    .encodedPath(invite.encodedPath!!)
                    .encodedQuery(invite.encodedQuery!!)
                    .build()

                val share = Intent.createChooser(Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, uri.toString())

                    putExtra(Intent.EXTRA_TITLE, "Group invitation link")

                    // (Optional) Here you're passing a content URI to an image to be displayed
//                    data = contentUri
                    type = "text/uri-list"
                    flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                }, null)

                Log.i(TAG, "onEvent: sending group invite url")
                ActivityQueue.sendIntent(share)
            }
        }
    }.let { }
}
