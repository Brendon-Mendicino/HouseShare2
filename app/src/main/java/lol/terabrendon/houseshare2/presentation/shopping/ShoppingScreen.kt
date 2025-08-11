package lol.terabrendon.houseshare2.presentation.shopping

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
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
import lol.terabrendon.houseshare2.domain.model.CheckoffStateModel
import lol.terabrendon.houseshare2.domain.model.ShoppingItemModel
import lol.terabrendon.houseshare2.presentation.provider.RegisterMenuAction
import lol.terabrendon.houseshare2.presentation.vm.ShoppingViewModel
import lol.terabrendon.houseshare2.util.fullFormat
import lol.terabrendon.houseshare2.util.splitAt
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

private const val TAG: String = "ShoppingScreen"

@Composable
fun ShoppingScreen(modifier: Modifier = Modifier, viewModel: ShoppingViewModel = hiltViewModel()) {
    val shoppingItems by viewModel.shoppingItems.collectAsStateWithLifecycle()
    val isAnySelected by viewModel.isAnySelected.collectAsStateWithLifecycle()
    val selectedItems by viewModel.selectedItems.collectAsStateWithLifecycle()
    val itemSorting by viewModel.itemSorting.collectAsStateWithLifecycle()
    var showDeleteDialog by rememberSaveable { mutableStateOf(false) }

    RegisterMenuAction(TAG) {
        AnimatedVisibility(visible = isAnySelected) {
            Row {
                IconButton(onClick = { showDeleteDialog = true }) {
                    Icon(imageVector = Icons.Filled.Delete, contentDescription = null)
                }

                IconButton(onClick = { viewModel.onEvent(ShoppingScreenEvent.ItemsCheckoff) }) {
                    Icon(imageVector = Icons.Filled.Checklist, contentDescription = null)
                }
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
        itemSorting = itemSorting,
        onEvent = viewModel::onEvent,
    )
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ShoppingScreenInner(
    modifier: Modifier = Modifier,
    shoppingItems: List<ShoppingItemModel>,
    itemSorting: ShoppingViewModel.ItemSorting,
    selectedItems: Set<Long>,
    onEvent: (ShoppingScreenEvent) -> Unit
) {
    val (checkedShoppingItems, shoppingItems) = shoppingItems
        .groupBy { it.checkoffState != null }
        .let { Pair(it[true] ?: emptyList(), it[false] ?: emptyList()) }

    val groupedCheckedItems = checkedShoppingItems.groupBy {
        it.checkoffState!!.checkoffTime.truncatedTo(ChronoUnit.DAYS)!!
    }

    LazyColumn(modifier = modifier.padding(8.dp)) {
        item {
            Text("Shopping list ordering:", fontStyle = FontStyle.Italic)

            SortingRow(
                modifier = modifier.padding(vertical = 8.dp),
                itemSorting = itemSorting,
                onEvent = onEvent,
            )

            HorizontalDivider()

        }

        items(
            items = shoppingItems.run {
                when (itemSorting) {
                    ShoppingViewModel.ItemSorting.CreationDate -> sortedByDescending { i -> i.info.creationTimestamp }
                    ShoppingViewModel.ItemSorting.Priority -> sortedByDescending { i -> i.info.priority }
                    ShoppingViewModel.ItemSorting.Name -> sortedByDescending { i -> i.info.name }
                    ShoppingViewModel.ItemSorting.Username -> sortedByDescending { i -> i.itemOwner.username }
                }
            },
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

        // Checked items
        groupedCheckedItems.forEach { day, checked ->
            stickyHeader {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    DateHeader(date = day)
                }
            }

            items(
                items = checked,
                key = { item -> item.info.id },
            ) { item ->
                ShoppingListItem(
                    shoppingItem = item,
                    onChecked = {},
                    selected = item.info.id in selectedItems,
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateItem()
                )
            }
        }
    }
}

@Composable
private fun DateHeader(modifier: Modifier = Modifier, date: LocalDateTime) {
    Card(modifier = modifier, elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
        Box(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)) {
            Text(date.fullFormat())
        }
    }
}

@Composable
private fun SortingRow(
    modifier: Modifier = Modifier,
    itemSorting: ShoppingViewModel.ItemSorting,
    onEvent: (ShoppingScreenEvent) -> Unit
) {
    LazyRow(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(ShoppingViewModel.ItemSorting.entries) { entry ->
            val selected = itemSorting == entry

            InputChip(
                selected = selected,
                onClick = {
                    onEvent(ShoppingScreenEvent.SortingChanged(entry))
                },
                label = {
                    Text(stringResource(entry.toStringRes()))
                },
                leadingIcon = {
                    AnimatedVisibility(selected) {
                        Icon(Icons.Filled.Done, contentDescription = null)
                    }
                }
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
private fun ShoppingScreenPreview() {
    val (l1, l2) = List(6) { ShoppingItemModel.default() }
        .mapIndexed { id, it ->
            it.copy(info = it.info.copy(id = id.toLong(), name = "Item"))
        }
        .splitAt(3)

    ShoppingScreenInner(
        shoppingItems = l1 + l2.map { it.copy(checkoffState = CheckoffStateModel.default()) },
        selectedItems = emptySet(),
        onEvent = {},
        itemSorting = ShoppingViewModel.ItemSorting.CreationDate,
    )
}