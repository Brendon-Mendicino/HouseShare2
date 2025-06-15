package lol.terabrendon.houseshare2.presentation.groups.form

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.InputChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import lol.terabrendon.houseshare2.domain.model.GroupFormState
import lol.terabrendon.houseshare2.domain.model.GroupFormStateValidator
import lol.terabrendon.houseshare2.domain.model.UserModel
import lol.terabrendon.houseshare2.domain.model.toValidator
import lol.terabrendon.houseshare2.presentation.components.AvatarIcon
import lol.terabrendon.houseshare2.presentation.vm.GroupFormViewModel

private const val TAG: String = "GroupUsersFormScreen"

@Composable
fun GroupUsersFormScreen(viewModel: GroupFormViewModel = hiltViewModel()) {
    val formState by viewModel.groupFormState.collectAsState()
    val users by viewModel.users.collectAsState()

    GroupUsersFormScreenInner(
        groupFormState = formState,
        users = users,
        onEvent = viewModel::onEvent,
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun GroupUsersFormScreenInner(
    groupFormState: GroupFormStateValidator,
    users: List<UserModel>,
    onEvent: (GroupFormEvent) -> Unit,
) {
    Log.d(TAG, "GroupUsersFormScreenInner: groupFormState=$groupFormState")
    var selectedChip by remember { mutableStateOf<Int?>(null) }
    var chipBounds by remember { mutableStateOf<Rect?>(null) }

    LazyColumn {
        // Selected users
        item {
            // This Box listens tab gesture outside of the current selected chip,
            // if there is a tap the current selected chip will be unselected
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(chipBounds) {
                        detectTapGestures { offset ->
                            val isInsideChip = chipBounds?.contains(offset) == true
                            if (!isInsideChip && selectedChip != null) {
                                selectedChip = null
                            }
                        }
                    },
            ) {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    groupFormState.users.value.forEachIndexed { index, user ->
                        SelectedUserItem(
                            user = user,
                            selected = index == selectedChip,
                            onSelected = {
                                selectedChip = index
                                chipBounds = it
                            },
                            onSelectedClick = { onEvent(GroupFormEvent.UserSelectedClicked(user.id)) },
                        )
                    }
                }
            }

            HorizontalDivider()
        }

        // List of users to select from
        items(users) { user ->
            UserListItem(user = user, onClick = { onEvent(GroupFormEvent.UserListClicked(it)) })
            HorizontalDivider()
        }
    }
}

@Composable
private fun UserListItem(user: UserModel, onClick: (UserModel) -> Unit) {
    Row(
        modifier = Modifier
            .padding(vertical = 8.dp, horizontal = 16.dp)
            .fillMaxWidth()
            .clickable {
                onClick(user)
            },
    ) {
        AvatarIcon(firstName = user.username)

        Spacer(Modifier.requiredWidth(16.dp))

        Text(text = user.username)
    }
}

@Composable
private fun SelectedUserItem(
    user: UserModel,
    selected: Boolean,
    onSelected: (chipBounds: Rect?) -> Unit,
    onSelectedClick: (UserModel) -> Unit,
) {
    var chipBounds by remember { mutableStateOf<Rect?>(null) }

    InputChip(
        modifier = Modifier
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
        trailingIcon = {
            Icon(Icons.Filled.Close, contentDescription = null)
        },
        avatar = {
            AvatarIcon(size = 24.dp, firstName = user.username)
        },
    )
}

@Preview(showBackground = true)
@Composable
fun GroupUsersFormScreenPreview() {
    GroupUsersFormScreenInner(
        groupFormState = GroupFormState(users = (0..4).map { UserModel.default() }).toValidator(),
        users = (0..4).map { UserModel.default() },
        onEvent = {},
    )
}