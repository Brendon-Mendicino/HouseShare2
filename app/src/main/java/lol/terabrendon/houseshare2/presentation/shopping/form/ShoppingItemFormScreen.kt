package lol.terabrendon.houseshare2.presentation.shopping.form

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType.Companion.PrimaryNotEditable
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import kotlinx.coroutines.launch
import lol.terabrendon.houseshare2.R
import lol.terabrendon.houseshare2.domain.model.ShoppingItemFormState
import lol.terabrendon.houseshare2.domain.model.ShoppingItemFormStateValidator
import lol.terabrendon.houseshare2.domain.model.ShoppingItemPriority
import lol.terabrendon.houseshare2.domain.model.toValidator
import lol.terabrendon.houseshare2.presentation.components.FormOutlinedTextField
import lol.terabrendon.houseshare2.presentation.components.RegisterBackNavIcon
import lol.terabrendon.houseshare2.presentation.navigation.HomepageNavigation
import lol.terabrendon.houseshare2.presentation.provider.FabConfig
import lol.terabrendon.houseshare2.presentation.provider.RegisterFabConfig
import lol.terabrendon.houseshare2.presentation.util.SnackbarController
import lol.terabrendon.houseshare2.presentation.util.SnackbarEvent
import lol.terabrendon.houseshare2.presentation.vm.ShoppingItemFormViewModel
import lol.terabrendon.houseshare2.util.ObserveAsEvent

@Composable
fun ShoppingItemFormScreen(
    viewModel: ShoppingItemFormViewModel = hiltViewModel(),
    onBack: () -> Unit,
) {
    val state by viewModel.formState.collectAsState()
    val scope = rememberCoroutineScope()

    ObserveAsEvent(viewModel.uiEvents) { event ->
        when (event) {
            is ShoppingItemFormUiEvent.SubmitFailure -> scope.launch {
                SnackbarController.sendEvent(SnackbarEvent(message = event.error))
            }

            ShoppingItemFormUiEvent.SubmitSuccess -> onBack()
        }
    }

    RegisterFabConfig<HomepageNavigation.ShoppingForm>(
        config = FabConfig.Fab(
            visible = true,
            icon = { Icon(Icons.Filled.Check, null) },
            onClick = { viewModel.onEvent(ShoppingItemFormEvent.Submit) },
        )
    )

    RegisterBackNavIcon<HomepageNavigation.ShoppingForm>(onClick = onBack)

    ShoppingItemFormScreenInner(
        state = state,
        onEvent = viewModel::onEvent,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ShoppingItemFormScreenInner(
    state: ShoppingItemFormStateValidator,
    onEvent: (ShoppingItemFormEvent) -> Unit,
) {
    val scrollState = rememberScrollState()
    var categoryExpended by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .padding(8.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        FormOutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            param = state.name,
            onValueChange = { onEvent(ShoppingItemFormEvent.NameChanged(it)) },
            labelText = stringResource(R.string.name),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
        )

        ExposedDropdownMenuBox(
            expanded = categoryExpended,
            onExpandedChange = { categoryExpended = it },
        ) {
            OutlinedTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .animateContentSize()
                    .menuAnchor(PrimaryNotEditable),
                readOnly = true,
                maxLines = 1,
                value = stringResource(state.priority.value.toStringRes()),
                onValueChange = {},
                leadingIcon = {
                    Icon(state.priority.value.toImageVector(), contentDescription = null)
                },
                label = { Text(stringResource(R.string.priority), maxLines = 1) },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(
                        expanded = categoryExpended,
                    )
                }
            )

            ExposedDropdownMenu(
                expanded = categoryExpended,
                onDismissRequest = { categoryExpended = false }
            ) {
                ShoppingItemPriority.entries.forEach { entry ->
                    DropdownMenuItem(
                        text = { Text(stringResource(entry.toStringRes()), maxLines = 1) },
                        leadingIcon = {
                            Icon(entry.toImageVector(), contentDescription = null)
                        },
                        onClick = {
                            onEvent(ShoppingItemFormEvent.PriorityChanged(entry))
                            categoryExpended = false
                        },
                        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                    )
                }
            }
        }

        FormOutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            param = state.amountStr,
            supportParams = listOf(state.amount),
            onValueChange = { onEvent(ShoppingItemFormEvent.AmountChanged(it)) },
            labelText = stringResource(R.string.amount),
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Next,
                keyboardType = KeyboardType.Number,
            ),
        )

        FormOutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            param = state.priceStr,
            supportParams = listOf(state.price),
            onValueChange = { onEvent(ShoppingItemFormEvent.PriceChanged(it)) },
            labelText = stringResource(R.string.price),
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Done,
                keyboardType = KeyboardType.Decimal,
            ),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ShoppingItemFormScreenPreview() {
    ShoppingItemFormScreenInner(
        state = ShoppingItemFormState().toValidator(),
        onEvent = {},
    )
}