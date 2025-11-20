package lol.terabrendon.houseshare2.presentation.groups

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import lol.terabrendon.houseshare2.R
import lol.terabrendon.houseshare2.domain.model.GroupInfoModel
import lol.terabrendon.houseshare2.presentation.navigation.HomepageNavigation
import lol.terabrendon.houseshare2.presentation.navigation.MainNavigation
import lol.terabrendon.houseshare2.presentation.provider.FabConfig
import lol.terabrendon.houseshare2.presentation.provider.RegisterFabConfig
import lol.terabrendon.houseshare2.presentation.vm.GroupsViewModel
import lol.terabrendon.houseshare2.util.ObserveAsEvent

@Composable
fun GroupsScreen(viewModel: GroupsViewModel = hiltViewModel(), navigate: (MainNavigation) -> Unit) {
    val groups by viewModel.groups.collectAsStateWithLifecycle()
    val selectedGroup by viewModel.selectedGroup.collectAsStateWithLifecycle()

    // Navigate when you select a new group
    ObserveAsEvent(viewModel.uiEvent) { event ->
        when (event) {
            is GroupsViewModel.UiEvent.GroupSelected ->
                if (event.groupId != null) {
                    navigate(HomepageNavigation.Shopping)
                }
        }
    }

    RegisterFabConfig<HomepageNavigation.Groups>(
        config = FabConfig.Fab(
            visible = true,
            expanded = true,
            text = "Groups",
            icon = { Icon(Icons.Filled.Add, null) },
            onClick = { navigate(HomepageNavigation.GroupUsersForm) }
        )
    )

    GroupsScreenInner(
        groups = groups,
        selectedGroup = selectedGroup,
        onEvent = viewModel::onEvent,
        onGroupClick = { navigate(HomepageNavigation.GroupInfo(it.groupId)) }
    )
}

@Composable
private fun GroupsScreenInner(
    modifier: Modifier = Modifier,
    groups: List<GroupInfoModel>,
    selectedGroup: GroupInfoModel?,
    onEvent: (GroupEvent) -> Unit,
    onGroupClick: (group: GroupInfoModel) -> Unit,
) {
    Column(modifier = modifier) {
        LazyColumn(
            modifier = Modifier
                .animateContentSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(
                groups,
                key = { it.groupId },
            ) { group ->
                GroupListItem(
                    modifier = Modifier.animateItem(),
                    group = group,
                    checked = group.groupId == selectedGroup?.groupId,
                    onCheckedToggle = { onEvent(GroupEvent.GroupSelected(group)) },
                    onGroupClick = onGroupClick,
                )
            }

            item {
                Spacer(Modifier.height(100.dp))
            }
        }
    }
}

@Composable
private fun GroupListItem(
    modifier: Modifier = Modifier,
    group: GroupInfoModel,
    checked: Boolean,
    onCheckedToggle: () -> Unit,
    onGroupClick: (group: GroupInfoModel) -> Unit,
) {
    Card(
        onClick = { onGroupClick(group) },
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Row(
            modifier = Modifier.padding(end = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Card {
                Image(
                    painter = painterResource(R.drawable.ic_launcher_background),
                    modifier = Modifier.size(80.dp),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                )
            }

            Column(
                modifier = Modifier.weight(1f),
            ) {
                Text(text = group.name, maxLines = 1, fontWeight = FontWeight.Bold)

                if (group.description != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(group.description, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }

            Switch(checked = checked, onCheckedChange = { onCheckedToggle() })
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun GroupsScreenPreview() {
    GroupsScreenInner(
        groups = (0..4).map { GroupInfoModel.default().copy(groupId = it.toLong()) },
        selectedGroup = GroupInfoModel.default(),
        onEvent = {},
        onGroupClick = {},
    )
}