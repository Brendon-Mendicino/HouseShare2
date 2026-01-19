package lol.terabrendon.houseshare2.presentation.groups.form

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.InputChip
import androidx.compose.material3.InputChipDefaults
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import lol.terabrendon.houseshare2.R
import lol.terabrendon.houseshare2.domain.form.GroupFormState
import lol.terabrendon.houseshare2.domain.form.GroupFormStateValidator
import lol.terabrendon.houseshare2.domain.form.toValidator
import lol.terabrendon.houseshare2.domain.model.UserModel
import lol.terabrendon.houseshare2.presentation.components.AvatarIcon
import lol.terabrendon.houseshare2.presentation.components.RegisterBackNavIcon
import lol.terabrendon.houseshare2.presentation.navigation.HomepageNavigation
import lol.terabrendon.houseshare2.presentation.provider.FabConfig
import lol.terabrendon.houseshare2.presentation.provider.RegisterFabConfig
import lol.terabrendon.houseshare2.presentation.vm.GroupFormViewModel
import lol.terabrendon.houseshare2.ui.theme.HouseShare2Theme
import timber.log.Timber

@Composable
fun GroupUsersFormScreen(
    viewModel: GroupFormViewModel = hiltViewModel(),
    onBack: () -> Unit,
    onNext: () -> Unit,
) {
    Timber.d("GroupUsersFormScreen: entering screen")

    val formState by viewModel.groupFormState.collectAsState()
    val users by viewModel.users.collectAsState()
    val selectedUsers by viewModel.selectedUsers.collectAsState()

    RegisterBackNavIcon<HomepageNavigation.GroupUsersForm>(onClick = onBack)

    RegisterFabConfig<HomepageNavigation.GroupUsersForm>(
        FabConfig.Fab(
            onClick = onNext,
        )
    )

    GroupUsersFormScreenInner(
        groupFormState = formState,
        users = users,
        selectedUsers = selectedUsers,
        onNext = onNext,
        onEvent = viewModel::onEvent,
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun GroupUsersFormScreenInner(
    groupFormState: GroupFormStateValidator,
    users: List<UserModel>,
    selectedUsers: Set<Long>,
    onNext: () -> Unit,
    onEvent: (GroupFormEvent) -> Unit,
) {
    var selectedChipUserId by remember { mutableStateOf<Long?>(null) }
    var chipBounds by remember { mutableStateOf<Rect?>(null) }

    // When the selectedUsers are updated, remove the selectedChip if it's not
    // contained in the users anymore
    LaunchedEffect(selectedUsers) {
        if (!selectedUsers.contains(selectedChipUserId))
            selectedChipUserId = null
    }

    if (users.isEmpty()) {
        NoFriends(onContinue = onNext)
        return
    }

    LazyColumn {
        // Selected users
        item {
            // This Box listens tab gesture outside of the current selected chip,
            // if there is a tap the current selected chip will be unselected
            Surface(
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(chipBounds) {
                        detectTapGestures { offset ->
                            val isInsideChip = chipBounds?.contains(offset) == true
                            if (!isInsideChip && selectedChipUserId != null) {
                                selectedChipUserId = null
                            }
                        }
                    },
            ) {
                FlowRow(
                    modifier = Modifier
                        .fillMaxSize()
                        .animateContentSize(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    groupFormState.users.value.forEach { user ->
                        key(user.id) {
                            SelectedUserItem(
                                modifier = Modifier.animateItem(),
                                user = user,
                                selected = user.id == selectedChipUserId,
                                onSelected = {
                                    selectedChipUserId = user.id
                                    chipBounds = it
                                },
                                onSelectedClick = {
                                    onEvent(
                                        GroupFormEvent.UserSelectedClicked(
                                            user.id
                                        )
                                    )
                                },
                            )
                        }
                    }
                }
            }

            HorizontalDivider()
        }

        // List of users to select from
        items(users, key = { it.id }) { user ->
            UserListItem(
                user = user,
                selected = selectedUsers.contains(user.id),
                onClick = { onEvent(GroupFormEvent.UserListClicked(it)) },
            )
            HorizontalDivider()
        }
    }
}

@Composable
private fun UserListItem(
    modifier: Modifier = Modifier,
    user: UserModel,
    selected: Boolean,
    onClick: (UserModel) -> Unit,
) {
    ListItem(
        modifier = modifier.clickable { onClick(user) },
        headlineContent = { Text(user.username) },
        leadingContent = {
            // Better selection indicator: swap avatar for checkmark or overlay
            Box {
                AvatarIcon(user = user)
                AnimatedVisibility(
                    visible = selected,
                    enter = scaleIn() + fadeIn(),
                    exit = scaleOut() + fadeOut()
                ) {
                    Icon(
                        Icons.Filled.CheckCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .size(18.dp)
                            .align(Alignment.BottomEnd)
                            .background(MaterialTheme.colorScheme.surface, CircleShape)
                    )
                }
            }
        },
        colors = ListItemDefaults.colors(
            containerColor = if (selected) MaterialTheme.colorScheme.surfaceVariant else Color.Transparent
        )
    )
}

@Composable
private fun SelectedUserItem(
    modifier: Modifier = Modifier,
    user: UserModel,
    selected: Boolean,
    onSelected: (chipBounds: Rect?) -> Unit,
    onSelectedClick: (UserModel) -> Unit,
) {
    var chipBounds by remember { mutableStateOf<Rect?>(null) }
    val backgroundColor by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceContainerLow,
        animationSpec = tween(300),
        label = "InputChipBackgroundColor"
    )

    InputChip(
        modifier = modifier
            .onGloballyPositioned { layoutCoordinates ->
                val localBounds = layoutCoordinates.boundsInRoot()
                chipBounds = Rect(
                    offset = Offset(localBounds.left, localBounds.top),
                    size = localBounds.size,
                )
            },
        selected = selected,
        onClick = {
            if (!selected) {
                onSelected(chipBounds)
            } else {
                onSelectedClick(user)
            }
        },
        label = {
            Text(text = user.username)
        },
        colors = InputChipDefaults.inputChipColors(
            containerColor = backgroundColor,
            selectedContainerColor = backgroundColor,
        ),
        trailingIcon = {
            AnimatedVisibility(visible = selected) {
                Icon(Icons.Filled.Close, contentDescription = null)
            }
        },
        avatar = {
            AvatarIcon(size = 24.dp, user = user)
        },
    )
}

@Composable
private fun NoFriends(modifier: Modifier = Modifier, onContinue: () -> Unit = {}) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.PersonAdd,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = stringResource(R.string.you_have_no_friends_yet),
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = stringResource(R.string.don_t_worry),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Primary Action
        Button(
            onClick = onContinue,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.create_group_anyway))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GroupUsersFormScreenPreview() {
    val selected = (0..3).map { UserModel.default().copy(id = it.toLong()) }
    GroupUsersFormScreenInner(
        groupFormState = GroupFormState(users = selected).toValidator(),
        users = (0..5).map { UserModel.default().copy(id = it.toLong()) },
        selectedUsers = selected.map { it.id }.toSet(),
        onNext = {},
        onEvent = {},
    )
}

@Preview(showBackground = true)
@Composable
private fun EmptyUsers() {
    HouseShare2Theme {
        GroupUsersFormScreenInner(
            groupFormState = GroupFormState().toValidator(),
            users = emptyList(),
            selectedUsers = emptySet(),
            onNext = {},
            onEvent = {},
        )
    }
}