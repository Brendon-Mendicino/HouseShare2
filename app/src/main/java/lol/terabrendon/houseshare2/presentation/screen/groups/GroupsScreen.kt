package lol.terabrendon.houseshare2.presentation.screen.groups

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
import lol.terabrendon.houseshare2.ui.theme.HouseShare2Theme
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
    if (groups.isEmpty()) {
        EmptyGroupState()
        return
    }

    LazyColumn(
        modifier = modifier.animateContentSize(),
        contentPadding = PaddingValues(16.dp), // Adds breathing room around the list
        verticalArrangement = Arrangement.spacedBy(12.dp), // slightly increased spacing for card separation
    ) {
        item {
            HeaderSection(groups.size)
            Spacer(modifier = Modifier.height(8.dp))
        }

        items(
            items = groups,
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
            Spacer(Modifier.height(80.dp)) // Bottom spacer for FABs or Navigation bars
        }
    }
}

@Composable
private fun HeaderSection(groupCount: Int) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Your Groups",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "You are part of $groupCount active groups",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun EmptyGroupState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // You can use an Icon or an Image here
        Icon(
            imageVector = Icons.Outlined.Group, // Or Groups
            contentDescription = null,
            modifier = Modifier.size(72.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.no_groups_yet),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.create_a_group_to_start_sharing_expenses_and_shopping_lists),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GroupListItem(
    modifier: Modifier = Modifier,
    group: GroupInfoModel,
    checked: Boolean,
    onCheckedToggle: () -> Unit,
    onGroupClick: (group: GroupInfoModel) -> Unit,
) {
    // Determine styles based on selection state
    val borderColor =
        if (checked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
    val containerColor =
        if (checked) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface

    OutlinedCard(
        onClick = { onGroupClick(group) },
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.outlinedCardColors(containerColor = containerColor),
        border = BorderStroke(1.dp, borderColor)
    ) {
        ListItem(
            // Make the ListItem transparent so the Card color shows through
            colors = ListItemDefaults.colors(containerColor = Color.Transparent),

            headlineContent = {
                Text(
                    text = group.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            },
            supportingContent = {
                // Only render supporting content if description exists
                if (!group.description.isNullOrBlank()) {
                    Text(
                        text = group.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2, // Allow 2 lines for better context
                        overflow = TextOverflow.Ellipsis
                    )
                }
            },
            leadingContent = {
                // Group Image/Icon
                Surface(
                    modifier = Modifier.size(56.dp), // Standard M3 list image size
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Image(
                        painter = painterResource(R.drawable.ic_launcher_background), // Replace with your actual loader
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            },
            trailingContent = {
                Switch(
                    checked = checked,
                    onCheckedChange = { onCheckedToggle() }
                )
            }
        )
    }
}

@Preview(showBackground = true, name = "With groups")
@Composable
private fun GroupsScreenPreview() {
    HouseShare2Theme {
        GroupsScreenInner(
            groups = (0..4).map { GroupInfoModel.default().copy(groupId = it.toLong()) },
            selectedGroup = GroupInfoModel.default(),
            onEvent = {},
            onGroupClick = {},
        )
    }
}

@Preview(showBackground = true, name = "Empty")
@Composable
private fun GroupsScreenPreview_Empty() {
    HouseShare2Theme {
        GroupsScreenInner(
            groups = listOf(),
            selectedGroup = GroupInfoModel.default(),
            onEvent = {},
            onGroupClick = {},
        )
    }
}
