package lol.terabrendon.houseshare2.presentation.shopping

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
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
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import lol.terabrendon.houseshare2.R
import lol.terabrendon.houseshare2.data.repository.ShoppingItemRepository
import lol.terabrendon.houseshare2.domain.mapper.toStringRes
import lol.terabrendon.houseshare2.domain.model.CheckoffStateModel
import lol.terabrendon.houseshare2.domain.model.ShoppingItemModel
import lol.terabrendon.houseshare2.presentation.components.ChooseGroup
import lol.terabrendon.houseshare2.presentation.navigation.HomepageNavigation
import lol.terabrendon.houseshare2.presentation.navigation.MainNavigation
import lol.terabrendon.houseshare2.presentation.provider.FabConfig
import lol.terabrendon.houseshare2.presentation.provider.RegisterFabConfig
import lol.terabrendon.houseshare2.presentation.vm.ShoppingViewModel
import lol.terabrendon.houseshare2.util.fullFormat
import lol.terabrendon.houseshare2.util.inlineFormat
import java.time.LocalDateTime

private const val TAG: String = "ShoppingScreen"

@Composable
fun ShoppingScreen(
    modifier: Modifier = Modifier,
    viewModel: ShoppingViewModel = hiltViewModel(),
    navigate: (MainNavigation) -> Unit,
) {
    val currentGroup by viewModel.currentGroup.collectAsStateWithLifecycle()
    val groupAvailable = currentGroup != null
    val shoppingItems by viewModel.shoppingItems.collectAsStateWithLifecycle()
    val checkedItems by viewModel.checkedItems.collectAsStateWithLifecycle()
    val isAnySelected by viewModel.isAnySelected.collectAsStateWithLifecycle()
    val selectedItems by viewModel.selectedItems.collectAsStateWithLifecycle()
    val itemSorting by viewModel.itemSorting.collectAsStateWithLifecycle()
    var showDeleteDialog by rememberSaveable { mutableStateOf(false) }

    RegisterFabConfig<HomepageNavigation.Shopping>(
        config = FabConfig.Toolbar(
            // TODO: when having a nice config management put the groupAvailable here
            visible = true,
            expanded = isAnySelected,
            content = { expanded ->
                IconButton(
                    onClick = { showDeleteDialog = true },
                    Modifier.focusProperties { canFocus = expanded },
                ) {
                    Icon(imageVector = Icons.Filled.Delete, contentDescription = null)
                }

                IconButton(
                    onClick = { viewModel.onEvent(ShoppingScreenEvent.ItemsCheckoff) },
                    Modifier.focusProperties { canFocus = expanded },
                ) {
                    Icon(imageVector = Icons.Filled.Checklist, contentDescription = null)
                }
            },
            fab = FabConfig.Fab(
                onClick = { navigate(HomepageNavigation.ShoppingForm) }.takeIf { groupAvailable }
            )
        )
    )

    if (!groupAvailable) {
        ChooseGroup(modifier = Modifier.fillMaxSize())
        return
    }

    ShoppingScreenInner(
        modifier = modifier,
        shoppingItems = shoppingItems,
        checkedItems = checkedItems,
        selectedItems = selectedItems,
        itemSorting = itemSorting,
        onEvent = viewModel::onEvent,
        onItemClick = { navigate(HomepageNavigation.ShoppingItem(it.info.id)) }
    )

    if (showDeleteDialog) {
        DeleteShoppingItemsDialog(
            onConfirm = {
                viewModel.onEvent(ShoppingScreenEvent.ItemsDeleted)
                showDeleteDialog = false
            },
            onDismiss = { showDeleteDialog = false },
        )
    }

}


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ShoppingScreenInner(
    modifier: Modifier = Modifier,
    shoppingItems: List<ShoppingItemModel>,
    checkedItems: List<ShoppingItemModel>,
    itemSorting: ShoppingItemRepository.Sorting,
    selectedItems: Set<Long>,
    onEvent: (ShoppingScreenEvent) -> Unit,
    onItemClick: (ShoppingItemModel) -> Unit,
) {
    var showCheckItems by rememberSaveable { mutableStateOf(true) }

    LazyColumn(
        modifier = modifier
            .padding(8.dp)
            .animateContentSize()
    ) {
        item {
            SortingRow(
                modifier = modifier.padding(vertical = 8.dp),
                itemSorting = itemSorting,
                onEvent = onEvent,
            )
        }

        items(
            items = shoppingItems,
            key = { item -> "${item.info.id}uncheck" },
        ) { item ->
            ShoppingListItem(
                shoppingItem = item,
                onChecked = { onEvent(ShoppingScreenEvent.ItemChecked(item.info)) },
                selected = item.info.id in selectedItems,
                onItemClick = onItemClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .animateItem()
            )
        }

        // Checked items
        item {
            AnimatedVisibility(checkedItems.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .padding(vertical = 8.dp)
                        .clickable { showCheckItems = !showCheckItems },
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(stringResource(R.string.selected_items), fontWeight = FontWeight.Bold)

                    HorizontalDivider(modifier = Modifier.weight(1f))

                    AnimatedContent(showCheckItems) { show ->
                        if (show)
                            Icon(Icons.Filled.ArrowDropDown, null, modifier = modifier.size(24.dp))
                        else
                            Icon(Icons.Filled.ArrowDropUp, null, modifier = modifier.size(24.dp))
                    }
                }
            }
        }

        if (showCheckItems) {
            items(
                items = checkedItems,
                key = { item -> "${item.info.id}check" },
            ) { item ->
                CheckedShoppingListItem(
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateItem(),
                    shoppingItem = item,
                    onItemClick = onItemClick,
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
    itemSorting: ShoppingItemRepository.Sorting,
    onEvent: (ShoppingScreenEvent) -> Unit,
) {
    LazyRow(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(ShoppingItemRepository.Sorting.entries) { entry ->
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
    onItemClick: (ShoppingItemModel) -> Unit,
    onChecked: () -> Unit,
) {
    val info = shoppingItem.info
    val user = shoppingItem.itemOwner

    Box(
        modifier.combinedClickable(
            onLongClick = {
                onChecked()
            },
            onClick = { onItemClick(shoppingItem) },
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
private fun CheckedShoppingListItem(
    modifier: Modifier = Modifier,
    shoppingItem: ShoppingItemModel,
    onItemClick: (ShoppingItemModel) -> Unit,
) {
    val info = shoppingItem.info
    val checkoff = shoppingItem.checkoffState!!

    CompositionLocalProvider(
        LocalContentColor provides MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
    ) {
        Row(
            modifier = modifier
                .clickable { onItemClick(shoppingItem) }
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
                Text(
                    info.name,
                    style = TextStyle(textDecoration = TextDecoration.LineThrough),
                )

                Row {
                    Text(
                        stringResource(R.string.bought_by, checkoff.checkoffUser.username),
                        fontStyle = FontStyle.Italic,
                        modifier = Modifier.weight(1f)
                    )

                    Spacer(Modifier.requiredWidth(12.dp))

                    Text(checkoff.checkoffTime.inlineFormat())
                }
            }

            Spacer(modifier = Modifier.requiredWidth(16.dp))

//            Checkbox(
//                checked = selected,
//                onCheckedChange = { onChecked() },
//            )
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
    val items = List(4) { ShoppingItemModel.default() }
        .mapIndexed { id, it ->
            it.copy(info = it.info.copy(id = id.toLong(), name = "Item"))
        }

    val checked = List(4) { ShoppingItemModel.default() }
        .map { it.copy(checkoffState = CheckoffStateModel.default()) }
        .mapIndexed { id, item ->
            item.copy(info = item.info.copy(id = id.toLong() + items.size, name = "Checked"))
        }

    ShoppingScreenInner(
        shoppingItems = items,
        selectedItems = emptySet(),
        checkedItems = checked,
        onEvent = {},
        itemSorting = ShoppingItemRepository.Sorting.CreationDate,
        onItemClick = {},
    )
}