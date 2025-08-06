package lol.terabrendon.houseshare2.presentation.shopping

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import lol.terabrendon.houseshare2.R
import lol.terabrendon.houseshare2.domain.model.ShoppingItemModel
import lol.terabrendon.houseshare2.presentation.provider.RegisterMenuAction
import lol.terabrendon.houseshare2.presentation.vm.ShoppingViewModel

private const val TAG: String = "ShoppingScreen"

@Composable
fun ShoppingScreen(modifier: Modifier = Modifier, viewModel: ShoppingViewModel = hiltViewModel()) {
    val shoppingItems by viewModel.shoppingItems.collectAsStateWithLifecycle()
    val isAnySelected by viewModel.isAnySelected.collectAsStateWithLifecycle()
    val selectedItems by viewModel.selectedItems.collectAsStateWithLifecycle()
    var showDeleteDialog by rememberSaveable { mutableStateOf(false) }

    RegisterMenuAction(TAG) {
        AnimatedVisibility(visible = isAnySelected) {
            IconButton(onClick = { showDeleteDialog = true }) {
                Icon(imageVector = Icons.Filled.Delete, contentDescription = null)
            }
        }
    }

    if (showDeleteDialog) {
        DeleteShoppingItemsDialog(
            onConfirm = {
                viewModel.onEvent(ShoppingScreenEvent.ItemsDeleted)
                showDeleteDialog = false
            },
            onDismiss = { showDeleteDialog = false },
        )
    }

    ShoppingScreenInner(
        modifier = modifier,
        shoppingItems = shoppingItems,
        selectedItems = selectedItems,
        onEvent = viewModel::onEvent,
    )
}


@Composable
fun ShoppingScreenInner(
    modifier: Modifier = Modifier,
    shoppingItems: List<ShoppingItemModel>,
    selectedItems: Set<Long>,
    onEvent: (ShoppingScreenEvent) -> Unit
) {

    LazyColumn(modifier = modifier) {
        items(
            items = shoppingItems,
            key = { item -> item.info.id }) { item ->
            ShoppingListItem(
                shoppingItem = item,
                onChecked = { onEvent(ShoppingScreenEvent.ItemChecked(item.info)) },
                selected = item.info.id in selectedItems,
                modifier = Modifier
                    .fillMaxWidth()
                    .animateItem()
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ShoppingListItem(
    modifier: Modifier = Modifier,
    shoppingItem: ShoppingItemModel,
    selected: Boolean,
    onChecked: () -> Unit,
) {
    val info = shoppingItem.info
    val user = shoppingItem.itemOwner

    Box(
        modifier.combinedClickable(
            onLongClick = {
                onChecked()
            },
            onClick = {},
        )
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .fillMaxWidth(),
        ) {
            Icon(
                imageVector = shoppingItem.info.priority.toImageVector(),
                contentDescription = null,
                modifier = Modifier.align(
                    Alignment.CenterVertically
                )
            )
            Spacer(modifier = Modifier.requiredWidth(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text("${info.name} • ${info.amount}")
                Text(
                    stringResource(R.string.created_by, user.username),
                    fontStyle = FontStyle.Italic
                )
            }

            Spacer(modifier = Modifier.requiredWidth(16.dp))

            Checkbox(
                checked = selected,
                onCheckedChange = { onChecked() },
            )
        }
    }
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
            Text(text = stringResource(R.string.the_current_selected_items_will_be_delete_permanently))
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

@Preview(showBackground = true)
@Composable
fun ShoppingScreenPreview() {
    ShoppingScreenInner(
        shoppingItems = List(6) {
            ShoppingItemModel.default()
        }.mapIndexed { id, it ->
            it.copy(info = it.info.copy(id = id.toLong(), name = "Item"))
        },
        selectedItems = emptySet(),
        onEvent = {},
    )
}