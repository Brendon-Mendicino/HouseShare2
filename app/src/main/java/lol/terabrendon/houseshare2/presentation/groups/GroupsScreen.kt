package lol.terabrendon.houseshare2.presentation.groups

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Group
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import lol.terabrendon.houseshare2.domain.model.GroupInfoModel
import lol.terabrendon.houseshare2.presentation.navigation.HomepageNavigation
import lol.terabrendon.houseshare2.presentation.navigation.MainNavigation
import lol.terabrendon.houseshare2.presentation.provider.FabConfig
import lol.terabrendon.houseshare2.presentation.provider.RegisterFabConfig
import lol.terabrendon.houseshare2.presentation.vm.GroupsViewModel

@Composable
fun GroupsScreen(viewModel: GroupsViewModel = hiltViewModel(), navigate: (MainNavigation) -> Unit) {
    val groups by viewModel.groups.collectAsState()
    val selectedGroup by viewModel.selectedGroup.collectAsState()

    RegisterFabConfig<HomepageNavigation.Groups>(
        config = FabConfig.Fab(
            visible = true,
            expanded = true,
            text = "Groups",
            icon = { Icon(Icons.Filled.Add, null) },
            onClick = { navigate(HomepageNavigation.GroupUsersForm) }
        )
    )

    GroupsScreenInner(groups = groups, selectedGroup = selectedGroup, onEvent = viewModel::onEvent)
}

@Composable
private fun GroupsScreenInner(
    groups: List<GroupInfoModel>,
    selectedGroup: GroupInfoModel?,
    onEvent: (GroupEvent) -> Unit,
) {
    Column {
        SelectedGroup(selectedGroup = selectedGroup, onEvent = onEvent)

        LazyColumn(
            modifier = Modifier
                .padding(bottom = 80.dp)
                .animateContentSize()
        ) {
            items(
                groups.filter { it.groupId != selectedGroup?.groupId },
                key = { it.groupId }) { group ->
                GroupListItem(
                    group = group,
                    onCheckedChange = { onEvent(GroupEvent.GroupSelected(group)) },
                    modifier = Modifier.animateItem(),
                )
                HorizontalDivider()
            }
        }
    }
}

@Composable
private fun SelectedGroup(selectedGroup: GroupInfoModel?, onEvent: (GroupEvent) -> Unit) {
    var visibleGroup by remember { mutableStateOf(selectedGroup ?: GroupInfoModel.default()) }

    LaunchedEffect(selectedGroup) {
        if (selectedGroup != null)
            visibleGroup = selectedGroup
    }

    AnimatedContent(
        visibleGroup,
        contentKey = { it.groupId }
    ) { group ->
        key(group.groupId) {
            AnimatedVisibility(
                visible = selectedGroup != null,
            ) {
                Surface(color = MaterialTheme.colorScheme.primaryContainer) {
                    GroupListItem(
                        group = group,
                        onCheckedChange = { onEvent(GroupEvent.GroupSelected(group)) },
                        defaultChecked = true
                    )
                }
            }
        }
    }
}

@Composable
private fun GroupListItem(
    modifier: Modifier = Modifier,
    group: GroupInfoModel,
    onCheckedChange: (Boolean) -> Unit,
    defaultChecked: Boolean = false,
) {
    var checked by remember { mutableStateOf(defaultChecked) }

    Box(modifier = modifier) {
        Row(
            modifier = Modifier
                .padding(vertical = 8.dp, horizontal = 16.dp)
                .fillMaxWidth(),
        ) {
            Icon(Icons.Filled.Group, contentDescription = null, modifier = Modifier.size(40.dp))

            Spacer(Modifier.requiredWidth(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(text = group.name, maxLines = 1)
                // TODO: add number of users text
                Text("Participants: !TODO")
            }

            Spacer(Modifier.requiredWidth(16.dp))

            Switch(
                checked = checked,
                onCheckedChange = {
                    checked = it
                    onCheckedChange(it)
                },
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun GroupsScreenPreview() {
    GroupsScreenInner(
        (0..4).map { GroupInfoModel.default().copy(groupId = it.toLong()) },
        selectedGroup = GroupInfoModel.default(),
        onEvent = {},
    )
}