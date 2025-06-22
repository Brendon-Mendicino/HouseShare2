package lol.terabrendon.houseshare2.presentation.shopping

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
import androidx.compose.material.icons.filled.FoodBank
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import lol.terabrendon.houseshare2.domain.model.ShoppingItemModel
import lol.terabrendon.houseshare2.presentation.vm.ShoppingViewModel

@Composable
fun ShoppingScreen(modifier: Modifier = Modifier, viewModel: ShoppingViewModel = hiltViewModel()) {
    val shoppingItems by viewModel.shoppingItems.collectAsStateWithLifecycle()

    ShoppingScreenInner(
        modifier = modifier,
        shoppingItems = shoppingItems,
        onEvent = viewModel::onEvent,
    )
}


@Composable
fun ShoppingScreenInner(
    modifier: Modifier = Modifier,
    shoppingItems: List<ShoppingItemModel>,
    onEvent: (ShoppingScreenEvent) -> Unit
) {

    LazyColumn(modifier = modifier) {
        items(shoppingItems, key = { item: ShoppingItemModel -> item.id }) { item ->
            ShoppingListItem(
                shoppingItem = item,
                onChecked = { onEvent(ShoppingScreenEvent.ItemChecked(item)) },
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
    onChecked: () -> Unit,
) {
    val spacerModifier = Modifier.requiredWidth(16.dp)

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
                .fillMaxWidth()
                .padding(0.dp, 8.dp)
        ) {
            Spacer(spacerModifier)
            Icon(
                imageVector = Icons.Filled.FoodBank,
                contentDescription = null,
                modifier = Modifier.align(
                    Alignment.CenterVertically
                )
            )
            Spacer(spacerModifier)

            Column {
                Text(shoppingItem.name)
                Text(shoppingItem.amount.toString())
            }

            Spacer(spacerModifier.weight(1f))
            Checkbox(
                checked = shoppingItem.selected,
                onCheckedChange = { onChecked() },
            )
            Spacer(spacerModifier)
        }
    }
}
