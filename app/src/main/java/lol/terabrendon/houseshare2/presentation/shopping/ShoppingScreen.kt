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
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.findViewTreeViewModelStoreOwner
import lol.terabrendon.houseshare2.domain.model.ShoppingItemModel
import lol.terabrendon.houseshare2.presentation.vm.ShoppingViewModel

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ShoppingScreen(modifier: Modifier = Modifier) {
    val shoppingViewModel: ShoppingViewModel =
        hiltViewModel(LocalView.current.findViewTreeViewModelStoreOwner()!!)

    val shoppingItems by shoppingViewModel.shoppingItems.collectAsStateWithLifecycle()

    Column(modifier) {
        LazyColumn {
            items(shoppingItems, key = { item: ShoppingItemModel -> item.id }) { item ->
                ShoppingListItem(
                    shoppingItem = item,
                    onChecked = { id, selected -> shoppingViewModel.onItemSelected(id, selected) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateItem()
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ShoppingListItem(
    modifier: Modifier = Modifier,
    shoppingItem: ShoppingItemModel,
    onChecked: (Long, Boolean) -> Unit,
) {
    val spacerModifier = Modifier.requiredWidth(16.dp)

    Box(
        modifier.combinedClickable(onLongClick = {
            onChecked(shoppingItem.id, !shoppingItem.selected)
        }, onClick = {})
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
                onCheckedChange = { onChecked(shoppingItem.id, !shoppingItem.selected) },
            )
            Spacer(spacerModifier)
        }
    }
}
