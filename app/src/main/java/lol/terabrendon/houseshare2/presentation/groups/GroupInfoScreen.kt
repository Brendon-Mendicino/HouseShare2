package lol.terabrendon.houseshare2.presentation.groups

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import lol.terabrendon.houseshare2.R
import lol.terabrendon.houseshare2.domain.model.GroupModel
import lol.terabrendon.houseshare2.domain.model.UserModel
import lol.terabrendon.houseshare2.presentation.components.AvatarIcon
import lol.terabrendon.houseshare2.presentation.components.LoadingOverlayScreen
import lol.terabrendon.houseshare2.presentation.navigation.MainNavigation
import lol.terabrendon.houseshare2.presentation.navigation.Navigator
import lol.terabrendon.houseshare2.presentation.vm.GroupInfoViewModel
import lol.terabrendon.houseshare2.ui.theme.HouseShare2Theme
import lol.terabrendon.houseshare2.util.ObserveAsEvent

@Composable
fun GroupInfoScreen(
    modifier: Modifier = Modifier,
    viewModel: GroupInfoViewModel = hiltViewModel(),
    navigator: Navigator<MainNavigation>,
) {
    val group by viewModel.groupInfo.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()

    ObserveAsEvent(viewModel.uiEvent) { event ->
        when (event) {
            is GroupInfoViewModel.UiEvent.NoGroupFound -> navigator.pop()
        }
    }

    if (group == null) {
        LoadingOverlayScreen()
        return
    }

    GroupInfoInner(
        modifier = modifier,
        group = group!!,
        currentUser = currentUser,
        onEvent = viewModel::onEvent,
    )
}

@Composable
private fun GroupInfoInner(
    modifier: Modifier = Modifier,
    group: GroupModel,
    currentUser: UserModel?,
    onEvent: (event: GroupInfoEvent) -> Unit,
) {
    LazyColumn(
        modifier = modifier, // This likely contains the Scaffold padding
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Group Header Section
        item {
            GroupHeaderCard(
                groupName = group.info.name,
                memberCount = group.users.size,
                onShareClick = { onEvent(GroupInfoEvent.ShareGroup) }
            )
        }

        // Members Section Title
        item {
            Text(
                text = stringResource(R.string.members),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 8.dp, top = 8.dp)
            )
        }

        // 3. User List
        items(group.users, key = { it.id }) { user ->
            UserListItem(user = user, isCurrentUser = currentUser?.id == user.id)
        }
    }
}

@Composable
fun GroupHeaderCard(
    groupName: String,
    memberCount: Int,
    onShareClick: () -> Unit,
) {
    // A surface container to group the top-level info
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = groupName,
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Text(
                        text = stringResource(R.string.active_members, memberCount),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                    )
                }
            }

            // Tonal Button is less aggressive than Filled, perfect for secondary actions
            FilledTonalButton(
                onClick = onShareClick,
                modifier = Modifier.align(Alignment.End)
            ) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.invite_members))
            }
        }
    }
}

@Composable
fun UserListItem(modifier: Modifier = Modifier, user: UserModel, isCurrentUser: Boolean) {
    // OutlinedCard gives a clean separation without heavy shadows
    OutlinedCard(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        ListItem(
            colors = ListItemDefaults.colors(
                containerColor = Color.Transparent // Let Card color show through
            ),
            leadingContent = {
                BadgedBox(badge = {
                    if (isCurrentUser) {
                        Badge {
                            Text("You")
                        }
                    }
                }) {
                    AvatarIcon(user = user)
                }
            },
            headlineContent = {
                Text(
                    text = user.username,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
            },
            supportingContent = {
                // TODO: Useful for context (e.g., "Joined 2 days ago" or email)
                // If you don't have this data yet, you can remove this parameter
                Text("Member", style = MaterialTheme.typography.labelMedium)
            }
        )
    }
}

@Preview(showBackground = true, name = "3 Users")
@Composable
fun PreviewGroupInfo_Populated() {
    HouseShare2Theme {
        val mockUsers = List(3) { UserModel.random() }
        val mockGroup = GroupModel.default().copy(users = mockUsers)

        GroupInfoInner(
            group = mockGroup,
            currentUser = mockUsers.first(),
            onEvent = {}
        )
    }
}

@Preview(showBackground = true, name = "0 Users")
@Composable
fun PreviewGroupInfo_Empty() {
    HouseShare2Theme {
        val mockGroup = GroupModel.default()

        GroupInfoInner(
            group = mockGroup,
            currentUser = null,
            onEvent = {}
        )
    }
}