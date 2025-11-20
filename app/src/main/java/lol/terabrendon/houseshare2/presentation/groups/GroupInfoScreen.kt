package lol.terabrendon.houseshare2.presentation.groups

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import lol.terabrendon.houseshare2.domain.model.GroupModel
import lol.terabrendon.houseshare2.domain.model.UserModel
import lol.terabrendon.houseshare2.presentation.components.AvatarIcon
import lol.terabrendon.houseshare2.presentation.components.LoadingOverlayScreen
import lol.terabrendon.houseshare2.presentation.navigation.MainNavigation
import lol.terabrendon.houseshare2.presentation.navigation.Navigator
import lol.terabrendon.houseshare2.presentation.vm.GroupInfoViewModel
import lol.terabrendon.houseshare2.util.ObserveAsEvent

@Composable
fun GroupInfoScreen(
    modifier: Modifier = Modifier,
    viewModel: GroupInfoViewModel = hiltViewModel(),
    navigator: Navigator<MainNavigation>,
) {
    val group by viewModel.groupInfo.collectAsStateWithLifecycle()

    ObserveAsEvent(viewModel.uiEvent) { event ->
        when (event) {
            is GroupInfoViewModel.UiEvent.NoGroupFound -> navigator.pop()
        }
    }

    if (group == null) {
        LoadingOverlayScreen()
        return
    }

    GroupInfoInner(modifier = modifier, group = group!!, onEvent = viewModel::onEvent)
}


@Composable
private fun GroupInfoInner(
    modifier: Modifier = Modifier,
    group: GroupModel,
    onEvent: (event: GroupInfoEvent) -> Unit,
) {
    LazyColumn(modifier = modifier) {

        item {

            Button(onClick = { onEvent(GroupInfoEvent.ShareGroup) }) {
                Text("Share")
            }
        }

        items(group.users, key = { it.id }) { user ->
            UserListItem(modifier = Modifier.fillMaxWidth(), user = user)
        }
    }
}

@Composable
fun UserListItem(modifier: Modifier = Modifier, user: UserModel) {
    Card(modifier = modifier) {
        Row(modifier = Modifier.padding(8.dp)) {
            AvatarIcon(user = user)

            Text(user.username)
        }
    }
}