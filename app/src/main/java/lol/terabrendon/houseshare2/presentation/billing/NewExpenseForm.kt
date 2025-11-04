package lol.terabrendon.houseshare2.presentation.billing

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType.Companion.PrimaryNotEditable
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import lol.terabrendon.houseshare2.R
import lol.terabrendon.houseshare2.domain.model.ExpenseCategory
import lol.terabrendon.houseshare2.domain.model.UserModel
import lol.terabrendon.houseshare2.presentation.vm.NewExpenseFormViewModel
import lol.terabrendon.houseshare2.util.ObserveAsEvent
import lol.terabrendon.houseshare2.util.currencyFormat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewExpenseForm(
    modifier: Modifier = Modifier,
    viewModel: NewExpenseFormViewModel = hiltViewModel(),
    onFinish: () -> Unit,
) {
    val expenseFormState by viewModel.expenseFormState.collectAsStateWithLifecycle()
    val payments by viewModel.payments.collectAsStateWithLifecycle()
    val users by viewModel.users.collectAsStateWithLifecycle()

    ObserveAsEvent(viewModel.finishedChannelFlow) {
        onFinish()
    }

    NewExpenseFormInner(
        modifier = modifier,
        expenseFormState = expenseFormState,
        users = users,
        payments = payments,
        onFinish = onFinish,
        onEvent = viewModel::onEvent,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewExpenseFormInner(
    modifier: Modifier = Modifier,
    expenseFormState: ExpenseFormState,
    users: List<UserModel>,
    payments: List<UserPaymentState>,
    onEvent: (ExpenseFormEvent) -> Unit = {},
    onFinish: () -> Unit = {},
) {
    var categoryExpended by remember { mutableStateOf(false) }
    var payerExpanded by remember { mutableStateOf(false) }


    val moneyAmount = expenseFormState.moneyAmount
    val description = expenseFormState.description
    val title = expenseFormState.title
    val category = expenseFormState.category
    val payer = expenseFormState.payer


    // TODO: in the future... handle errors from the viewmodel
    val amountError = { moneyAmount <= 0.0 }
    val categoryError = { category == null }
    val payerError = { payer == null }
    val titleError = { title.isEmpty() }
    val paymentsMoneyError = { payments.any { it.amountMoney < 0 } }
    val moneySumError =
        { payments.sumOf { it.amountMoney }.currencyFormat() != moneyAmount.currencyFormat() }

    val errors =
        listOf(
            amountError,
            categoryError,
            payerError,
            titleError,
            paymentsMoneyError,
            moneySumError,
        )

    LazyColumn(
        modifier.padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            FormTextField(
                value = if (moneyAmount != 0.0) moneyAmount.currencyFormat() else "",
                onValueChange = {
                    onEvent(
                        ExpenseFormEvent.MoneyAmountChanged(
                            it.currencyFormat() ?: 0.0
                        )
                    )
                },
                isError = amountError,
                errorText = {
                    Text(stringResource(R.string.amount_should_be_greater_than_0))
                },
                label = { Text(stringResource(R.string.amount), maxLines = 1) },
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Next,
                    keyboardType = KeyboardType.Decimal,
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            FormTextField(
                value = title,
                onValueChange = { onEvent(ExpenseFormEvent.TitleChanged(it)) },
                label = { Text(stringResource(R.string.title), maxLines = 1) },
                isError = titleError,
                errorText = { Text(stringResource(R.string.title_should_not_be_empty)) },
                maxLines = 1,
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Next,
                ),
                modifier = Modifier.fillMaxWidth()
            )

        }

        item {
            ExposedDropdownMenuBox(
                expanded = categoryExpended,
                onExpandedChange = { categoryExpended = it },
            ) {
                var used by rememberSaveable { mutableStateOf(false) }
                val isError = if (!used) false else categoryError()

                OutlinedTextField(
                    modifier = Modifier
                        .animateContentSize()
                        .menuAnchor(PrimaryNotEditable),
                    readOnly = true,
                    maxLines = 1,
                    value = category?.let { stringResource(it.toStringRes()) } ?: "",
                    onValueChange = {},
                    leadingIcon = category?.let {
                        {
                            Icon(it.toImageVector(), contentDescription = null)
                        }
                    },
                    isError = isError,
                    supportingText = if (!isError) null else ({
                        Text(stringResource(R.string.you_should_choose_a_category))
                    }),
                    label = { Text(stringResource(R.string.category), maxLines = 1) },
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
                    ExpenseCategory.entries.forEach { entry ->
                        DropdownMenuItem(
                            text = { Text(stringResource(entry.toStringRes()), maxLines = 1) },
                            leadingIcon = {
                                Icon(entry.toImageVector(), contentDescription = null)
                            },
                            onClick = {
                                onEvent(ExpenseFormEvent.CategoryChanged(if (entry == category) null else entry))
                                categoryExpended = false
                                used = true
                            },
                            contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                        )
                    }
                }
            }
        }

        item {
            ExposedDropdownMenuBox(
                expanded = payerExpanded,
                onExpandedChange = { payerExpanded = it }
            ) {
                var used by rememberSaveable { mutableStateOf(false) }
                val isError = if (!used) false else payerError()

                OutlinedTextField(
                    modifier = Modifier
                        .animateContentSize()
                        .menuAnchor(PrimaryNotEditable),
                    readOnly = true,
                    maxLines = 1,
                    value = payer?.username ?: "",
                    onValueChange = {},
                    isError = isError,
                    supportingText = if (!isError) null else ({
                        Text(stringResource(R.string.you_should_choose_a_payer))
                    }),
                    label = { Text(stringResource(R.string.payed_by)) },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(
                            expanded = payerExpanded,
                        )
                    }
                )

                ExposedDropdownMenu(
                    expanded = payerExpanded,
                    onDismissRequest = { payerExpanded = false }
                ) {
                    users.forEach { user ->
                        DropdownMenuItem(
                            text = { Text(user.username, maxLines = 1) },
                            onClick = {
                                onEvent(ExpenseFormEvent.PayerChanged(user))
                                payerExpanded = false
                                used = true
                            },
                            contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                        )
                    }
                }
            }
        }

        item {
            FormTextField(
                value = description ?: "",
                onValueChange = { onEvent(ExpenseFormEvent.DescriptionChanged(it)) },
                label = {
                    Text(stringResource(R.string.description))
                },
                minLines = 3,
                maxLines = 10,
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Next,
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }

        // List of contributors

        item {
            HorizontalDivider()
        }

        // Draw the list of payments
        divisionListForm(
            payments = payments,
            onUpdateUnit = { index, unit ->
                onEvent(ExpenseFormEvent.UnitChanged(index, unit))
            },
            onValueUnitChange = { index, value ->
                onEvent(ExpenseFormEvent.ValueUnitChanged(index, value))
            }
        )


        item {
            Column(modifier = Modifier.animateContentSize()) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TextButton(onClick = { onFinish() }) {
                        Text(stringResource(R.string.dismiss))
                    }

                    TextButton(
                        onClick = {
                            onEvent(ExpenseFormEvent.Submit)
                        },
                        enabled = !errors.any { isError -> isError() },
                    ) {
                        Text(stringResource(R.string.confirm))
                    }
                }

                if (moneySumError()) {
                    Text(
                        stringResource(R.string.the_sum_of_the_money_should_equal_the_total_amount),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
private fun FormTextField(
    modifier: Modifier = Modifier,
    label: (@Composable () -> Unit)? = null,
    errorText: (@Composable () -> Unit)? = null,
    isError: () -> Boolean = { false },
    value: String,
    onValueChange: (String) -> Unit,
    minLines: Int = 1,
    maxLines: Int = 1,
    keyboardOptions: KeyboardOptions = KeyboardOptions(),
) {
    var used by rememberSaveable { mutableStateOf(false) }
    val error = if (!used) false else isError()

    OutlinedTextField(
        value = value,
        onValueChange = {
            onValueChange(it)
            used = true
        },
        trailingIcon = if (error) ({
            Icon(
                Icons.Filled.Error,
                stringResource(R.string.error),
                tint = MaterialTheme.colorScheme.error
            )
        })
        else if (value != "") ({
            IconButton(onClick = { onValueChange("") }) {
                Icon(imageVector = Icons.Outlined.Cancel, contentDescription = null)
            }
        }) else null,
        supportingText = if (error) errorText else null,
        keyboardOptions = keyboardOptions,
        minLines = minLines,
        maxLines = maxLines,
        label = label,
        isError = error,
        modifier = modifier.animateContentSize(),
    )
}

@OptIn(ExperimentalMaterial3Api::class)
private fun LazyListScope.divisionListForm(
    payments: List<UserPaymentState>,
    onUpdateUnit: (index: Int, newUnit: PaymentUnit) -> Unit,
    onValueUnitChange: (index: Int, newValue: String?) -> Unit,
) {
    itemsIndexed(payments) { index, payment ->
        var expanded by rememberSaveable { mutableStateOf(false) }

        val valueError = { payment.amountMoney < 0 }
        val errors = listOf(valueError)
        val error = errors.any { isError -> isError() }

        Box {
            OutlinedTextField(
                modifier = Modifier
                    .animateContentSize()
                    .fillMaxWidth(),
                maxLines = 1,
                value = payment.amountUnit,
                onValueChange = { newValue ->
                    onValueUnitChange(index, newValue)
                },
                leadingIcon = {
                    IconButton(onClick = { expanded = !expanded }) {
                        Icon(payment.unit.toImageVector(), contentDescription = null)
                    }
                },
                suffix = {
                    Text(payment.amountMoney.currencyFormat())
                },
                isError = error,
                label = { Text(payment.user.username) },
                trailingIcon = {
                    if (!error)
                        return@OutlinedTextField

                    Icon(
                        Icons.Filled.Error,
                        stringResource(R.string.error),
                        tint = MaterialTheme.colorScheme.error
                    )
                },
                supportingText = if (!error) null else ({
                    Text(stringResource(R.string.amount_should_be_greater_than_0))
                }),
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Next,
                    keyboardType = KeyboardType.Decimal,
                )
            )

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                PaymentUnit.entries.forEach { entry ->
                    DropdownMenuItem(
                        text = { Text(stringResource(entry.toStringRes()), maxLines = 1) },
                        leadingIcon = {
                            Icon(entry.toImageVector(), contentDescription = null)
                        },
                        onClick = {
                            onUpdateUnit(index, entry)
                            expanded = false
                        },
                        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                    )
                }
            }
        }
    }
}

@Preview(showSystemUi = true, showBackground = true)
@Composable
fun FormPreview(
    state: ExpenseFormState = ExpenseFormState(),
    payments: List<UserPaymentState> = (0..2).map { UserPaymentState.default() }
) {
    NewExpenseFormInner(expenseFormState = state, payments = payments, users = emptyList())
}

@Preview(showBackground = true)
@Composable
fun UserListPreview() {
    val payments = rememberSaveable {
        mutableStateListOf(*(0..5).map {
            UserPaymentState.default()
        }.toTypedArray())
    }

    LazyColumn {
        divisionListForm(
            payments = payments,
            onUpdateUnit = { _, _ -> },
            onValueUnitChange = { _, _ -> })
    }
}