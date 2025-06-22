package lol.terabrendon.houseshare2.presentation.shopping.form

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import lol.terabrendon.houseshare2.R
import lol.terabrendon.houseshare2.domain.model.ShoppingItemFormState
import lol.terabrendon.houseshare2.domain.model.ShoppingItemFormStateValidator
import lol.terabrendon.houseshare2.domain.model.toValidator
import lol.terabrendon.houseshare2.presentation.components.FormOutlinedTextField
import lol.terabrendon.houseshare2.presentation.util.SnackbarController
import lol.terabrendon.houseshare2.presentation.util.SnackbarEvent
import lol.terabrendon.houseshare2.presentation.util.errorText
import lol.terabrendon.houseshare2.presentation.vm.ShoppingItemFormViewModel
import lol.terabrendon.houseshare2.util.ObserveAsEvent

@Composable
fun ShoppingItemFormScreen(
    viewModel: ShoppingItemFormViewModel = hiltViewModel(),
    navController: NavController
) {
    val state by viewModel.formState.collectAsState()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    ObserveAsEvent(viewModel.uiEvents) { event ->
        when (event) {
            is ShoppingItemFormUiEvent.SubmitFailure -> scope.launch {
                SnackbarController.sendEvent(
                    SnackbarEvent(
                        message = event.error.errorText(
                            "",
                            context
                        )
                    )
                )
            }

            ShoppingItemFormUiEvent.SubmitSuccess -> navController.popBackStack()
        }
    }

    ShoppingItemFormScreenInner(
        state = state,
        onEvent = viewModel::onEvent,
        onBack = { navController.popBackStack() }
    )
}

@Composable
private fun ShoppingItemFormScreenInner(
    state: ShoppingItemFormStateValidator,
    onEvent: (ShoppingItemFormEvent) -> Unit,
    onBack: () -> Unit,
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
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

        FormOutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            param = state.name,
            onValueChange = { onEvent(ShoppingItemFormEvent.NameChanged(it)) },
            labelText = stringResource(R.string.name),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
        )

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

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
            modifier = Modifier.fillMaxWidth()
        ) {
            TextButton(onClick = { onBack() }) {
                Text(stringResource(R.string.dismiss))
            }

            TextButton(
                onClick = { onEvent(ShoppingItemFormEvent.Submit) },
                enabled = !state.isError
            ) {
                Text(stringResource(R.string.confirm))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ShoppingItemFormScreenPreview() {
    ShoppingItemFormScreenInner(
        state = ShoppingItemFormState().toValidator(),
        onBack = {},
        onEvent = {},
    )
}