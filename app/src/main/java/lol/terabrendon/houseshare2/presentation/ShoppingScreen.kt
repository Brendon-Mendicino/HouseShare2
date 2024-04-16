package lol.terabrendon.houseshare2.presentation

import android.icu.text.NumberFormat
import android.icu.util.Currency
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.FoodBank
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.findViewTreeViewModelStoreOwner
import lol.terabrendon.houseshare2.R
import lol.terabrendon.houseshare2.model.ShoppingItemModel
import lol.terabrendon.houseshare2.presentation.vm.ShoppingViewModel
import java.time.LocalDateTime

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
                        .animateItemPlacement()
                )
            }
        }
    }
}

private val currencyFormat = NumberFormat.getCurrencyInstance().apply {
    maximumFractionDigits = 2
    currency = Currency.getInstance("EUR")
}

@Composable
fun ShoppingItemForm(
    modifier: Modifier = Modifier,
    onFinish: (ShoppingItemModel) -> Unit,
    onBack: () -> Unit
) {
    val textModifier = Modifier.fillMaxWidth()
    var name by rememberSaveable { mutableStateOf("") }
    var amount by rememberSaveable { mutableIntStateOf(0) }
    var price by rememberSaveable { mutableStateOf<Double?>(null) }

    Column(modifier.padding(8.dp)) {
        FilledTonalIconButton(onClick = { onBack() }) {
            Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = null)
        }
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            trailingIcon = {
                if (name == "") return@OutlinedTextField

                IconButton(onClick = { name = "" }) {
                    Icon(imageVector = Icons.Outlined.Cancel, contentDescription = null)
                }
            },
            label = {
                Text(stringResource(R.string.name))
            },
            modifier = textModifier
        )
        OutlinedTextField(
            value = if (amount != 0) amount.toString() else "",
            onValueChange = { it.toIntOrNull()?.let { newAmount -> amount = newAmount } },
            trailingIcon = {
                if (amount == 0) return@OutlinedTextField

                IconButton(onClick = { amount = 0 }) {
                    Icon(imageVector = Icons.Outlined.Cancel, contentDescription = null)
                }
            },
            label = {
                Text(stringResource(R.string.amount))
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = textModifier
        )
        OutlinedTextField(
            value = price?.let { currencyFormat.format(it) } ?: "",
            onValueChange = { newPrice ->
                price = currencyFormat.runCatching { parse(newPrice).toDouble() }.getOrNull()
            },
            trailingIcon = {
                if (price == null) return@OutlinedTextField

                IconButton(onClick = { price = null }) {
                    Icon(imageVector = Icons.Outlined.Cancel, contentDescription = null)
                }
            },
            label = {
                Text(stringResource(R.string.price))
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = textModifier
        )

        Button(onClick = {
            onFinish(
                ShoppingItemModel(
                    id = 0,
                    name = name,
                    amount = amount,
                    price = price,
                    creationTimestamp = LocalDateTime.now(),
                    selected = false,
                )
            )
        }, modifier = Modifier.align(Alignment.End)) {
            Text("lkjasdlkfj")
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ShoppingListItem(
    modifier: Modifier = Modifier,
    shoppingItem: ShoppingItemModel,
    onChecked: (Int, Boolean) -> Unit,
) {
    val spacerModifier = Modifier.requiredWidth(16.dp)

    Box(
        modifier
            .combinedClickable(
                onLongClick = {
                    onChecked(shoppingItem.id, !shoppingItem.selected)
                },
                onClick = {})
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

@Preview(showSystemUi = true, showBackground = true)
@Composable
fun FormPreview() {
    ShoppingItemForm(onFinish = {}, onBack = {}, modifier = Modifier.fillMaxSize())
}