package lol.terabrendon.houseshare2.presentation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.findViewTreeViewModelStoreOwner
import lol.terabrendon.houseshare2.R
import lol.terabrendon.houseshare2.presentation.navigation.MainNavigation
import lol.terabrendon.houseshare2.presentation.vm.ShoppingViewModel

private enum class DialogKind {
    Shopping,
}

@Composable
fun AppBarActions(
    mainNavigation: MainNavigation,
) {
    var showDialog by rememberSaveable { mutableStateOf<DialogKind?>(null) }

    val shoppingViewModel: ShoppingViewModel =
        hiltViewModel(LocalView.current.findViewTreeViewModelStoreOwner()!!)

    val selected by shoppingViewModel.isAnySelected.collectAsStateWithLifecycle()

    when (showDialog) {
        null -> {}
        DialogKind.Shopping -> DeleteShoppingItemsDialog(
            onDismiss = { showDialog = null },
            onConfirm = {
                shoppingViewModel.onDeleteSelected()
                showDialog = null
            })
    }

    when (mainNavigation) {
        is MainNavigation.Shopping -> {
            if (!selected) return

            IconButton(onClick = { showDialog = DialogKind.Shopping }) {
                Icon(imageVector = Icons.Filled.Delete, contentDescription = null)
            }
        }

        else -> {}
    }

    // App Menu
    AppMenu()
}

@Composable
private fun DeleteShoppingItemsDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        icon = {
            Icon(
                Icons.Filled.Delete,
                contentDescription = stringResource(R.string.delete_shopping_items)
            )
        },
        title = {
            Text(text = stringResource(R.string.delete_shopping_items))
        },
        text = {
            Text(text = "The current selected items will be delete permanently.")
        },
        onDismissRequest = {
            onDismiss()
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm()
                }
            ) {
                Text(stringResource(R.string.confirm))
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    onDismiss()
                }
            ) {
                Text(stringResource(R.string.dismiss))
            }
        }
    )
}