package lol.terabrendon.houseshare2.presentation.shopping

import android.icu.text.NumberFormat
import android.icu.util.Currency
import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.FoodBank
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.findViewTreeViewModelStoreOwner
import lol.terabrendon.houseshare2.R
import lol.terabrendon.houseshare2.domain.model.ShoppingItemModel
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
                        .animateItem()
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
    modifier: Modifier = Modifier, onFinish: (ShoppingItemModel) -> Unit, onBack: () -> Unit
) {
    val textModifier = Modifier.fillMaxWidth()
    val scrollState = rememberScrollState()
    var name by rememberSaveable { mutableStateOf("") }
    var amount by rememberSaveable { mutableIntStateOf(0) }
    var price by rememberSaveable { mutableStateOf<Double?>(null) }

    val nameError = { name.isEmpty() }
    val amountError = { amount <= 0 }
    val errors = listOf(nameError, amountError)

    Column(
        modifier
            .padding(8.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            FilledTonalIconButton(onClick = { onBack() }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = null
                )
            }

            Spacer(Modifier.requiredWidth(8.dp))

            Text("Add new shopping item", style = MaterialTheme.typography.headlineSmall)
        }


        FormTextField(
            value = name,
            onValueChange = { name = it },
            label = {
                Text(stringResource(R.string.name))
            },
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Next
            ),
            isError = { nameError() },
            errorText = {
                Text(stringResource(R.string.name_should_not_be_empty))
            },
            modifier = textModifier
        )
        FormTextField(
            value = if (amount != 0) amount.toString() else "",
            onValueChange = {
                amount = it.toIntOrNull() ?: 0
            },
            label = {
                Text(stringResource(R.string.amount))
            },
            isError = { amountError() },
            errorText = {
                Text(stringResource(R.string.amount_should_be_greater_than_0))
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next
            ),
            modifier = textModifier
        )
        FormTextField(
            value = price?.let { currencyFormat.format(it) } ?: "",
            onValueChange = { newPrice ->
                price = currencyFormat.runCatching { parse(newPrice).toDouble() }.getOrNull()
            },
            label = {
                Text(stringResource(R.string.price))
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number, imeAction = ImeAction.Done
            ),
            modifier = textModifier
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
            modifier = Modifier.fillMaxWidth()
        ) {
            TextButton(onClick = { onBack() }) {
                Text(stringResource(R.string.dismiss))
            }

            TextButton(onClick = {
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
            }, enabled = !errors.any { isError -> isError() }) {
                Text(stringResource(R.string.confirm))
            }
        }
    }
}

@Composable
private fun FormTextField(
    modifier: Modifier = Modifier,
    label: @Composable () -> Unit = {},
    errorText: @Composable () -> Unit = {},
    isError: () -> Boolean = { false },
    keyboardOptions: KeyboardOptions = KeyboardOptions(),
    onValueChange: (String) -> Unit,
    value: String,
) {
    var used by rememberSaveable { mutableStateOf(false) }
    val error = if (!used) false else isError()

    OutlinedTextField(
        value = value,
        onValueChange = {
            onValueChange(it)
            used = true
        },
        trailingIcon = {
            if (error) {
                Icon(
                    Icons.Filled.Error,
                    stringResource(R.string.error),
                    tint = MaterialTheme.colorScheme.error
                )
                return@OutlinedTextField
            }

            if (value == "") return@OutlinedTextField

            IconButton(onClick = { onValueChange("") }) {
                Icon(imageVector = Icons.Outlined.Cancel, contentDescription = null)
            }
        },
        supportingText = {
            if (error) {
                errorText()
            }
        },
        keyboardOptions = keyboardOptions,
        label = { label() },
        isError = error,
        modifier = modifier,
    )
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

@Preview(showSystemUi = true, showBackground = true)
@Composable
fun FormPreview() {
    ShoppingItemForm(onFinish = {}, onBack = {}, modifier = Modifier.fillMaxSize())
}