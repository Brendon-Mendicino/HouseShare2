package lol.terabrendon.houseshare2.presentation.home

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import lol.terabrendon.houseshare2.R
import lol.terabrendon.houseshare2.domain.model.UserModel
import lol.terabrendon.houseshare2.presentation.vm.AppMenuViewModel

private enum class MenuDialog {
    AddUser,
    AddGroup,
}

@Composable
fun AppMenu(modifier: Modifier = Modifier) {
    val appMenuViewModel: AppMenuViewModel = hiltViewModel()

    var menuExpanded by remember { mutableStateOf(false) }
    var showDialog by remember { mutableStateOf<MenuDialog?>(null) }

    when (showDialog) {
        null -> {}
        MenuDialog.AddUser -> AddUserDialog(
            onDismiss = { showDialog = null },
            onConfirm = {
                appMenuViewModel.onInsertUser(it)
                showDialog = null
            })

        MenuDialog.AddGroup -> {}
    }

    IconButton(onClick = { menuExpanded = true }, modifier = modifier) {
        Icon(
            imageVector = Icons.Filled.MoreVert,
            contentDescription = stringResource(R.string.open_app_menu)
        )
    }
    DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
        DropdownMenuItem(
            text = { Text("Add user") },
            leadingIcon = {
                Icon(imageVector = Icons.Filled.PersonAdd, contentDescription = null)
            },
            onClick = { showDialog = MenuDialog.AddUser }
        )
    }
}

@Composable
private fun AddUserDialog(
    modifier: Modifier = Modifier, onDismiss: () -> Unit, onConfirm: (UserModel) -> Unit
) {
    var username by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = { onDismiss() },
        confirmButton = {
            TextButton(onClick = {
                onConfirm(
                    UserModel(id = 0, username = username)
                )
            }) {
                Text(stringResource(R.string.confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = { onDismiss() }) {
                Text(stringResource(R.string.dismiss))
            }
        },
        text = {
            TextField(value = username, onValueChange = { username = it })
        },
        modifier = modifier
    )
}
