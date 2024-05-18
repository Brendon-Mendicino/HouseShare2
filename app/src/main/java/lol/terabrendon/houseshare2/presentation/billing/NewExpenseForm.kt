package lol.terabrendon.houseshare2.presentation.billing

import androidx.compose.animation.animateContentSize
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
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import lol.terabrendon.houseshare2.model.ExpenseCategory
import lol.terabrendon.houseshare2.model.UserModel
import lol.terabrendon.houseshare2.presentation.vm.NewExpenseFormViewModel
import lol.terabrendon.houseshare2.util.currencyFormat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewExpenseForm(
    modifier: Modifier = Modifier,
    newExpenseFormViewModel: NewExpenseFormViewModel = hiltViewModel(),
    onFinish: () -> Unit,
) {
    var categoryExpended by remember { mutableStateOf(false) }

    val moneyAmount by newExpenseFormViewModel.moneyAmount.collectAsStateWithLifecycle()
    val description by newExpenseFormViewModel.description.collectAsStateWithLifecycle()
    val title by newExpenseFormViewModel.title.collectAsStateWithLifecycle()
    val category by newExpenseFormViewModel.category.collectAsStateWithLifecycle()
    val payments by newExpenseFormViewModel.payments.collectAsStateWithLifecycle()
    val isFinished by newExpenseFormViewModel.isFinished.collectAsStateWithLifecycle()

    val amountError = { moneyAmount <= 0.0 }
    val categoryError = { category == null }
    val titleError = { title.isEmpty() }
    val paymentsMoneyError = { payments.any { it.amountMoney < 0 } }
    val paymentsUnitError = { payments.any { it.amountUnit < 0 } }
    val moneySumError =
        { payments.sumOf { it.amountMoney }.currencyFormat() != moneyAmount.currencyFormat() }
    val errors =
        listOf(
            amountError,
            categoryError,
            titleError,
            paymentsMoneyError,
            paymentsUnitError,
            moneySumError,
        )

    LaunchedEffect(isFinished) {
        if (isFinished) onFinish()
    }

    LazyColumn(
        modifier.padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                FilledTonalIconButton(onClick = { onFinish() }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = null
                    )
                }

                Spacer(Modifier.requiredWidth(8.dp))

                Text("Add new shopping item", style = MaterialTheme.typography.headlineSmall)
            }
        }

        item {
            FormTextField(
                value = if (moneyAmount != 0.0) moneyAmount.currencyFormat() else "",
                onValueChange = {
                    newExpenseFormViewModel.onMoneyAmountChange(
                        it.currencyFormat() ?: 0.0
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
                onValueChange = { newExpenseFormViewModel.onTitleChange(it) },
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
                        .menuAnchor(),
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
                                newExpenseFormViewModel.onCategoryChange(if (entry == category) null else entry)
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
            FormTextField(
                value = description ?: "",
                onValueChange = { newExpenseFormViewModel.onDescriptionChange(it) },
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


        // Draw the list of payments
        divisionListForm(
            payments = payments,
            onUpdateUnit = newExpenseFormViewModel::onUnitChange,
            onValueUnitChange = newExpenseFormViewModel::onValueUnitChange
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
                            newExpenseFormViewModel.onConfirm()
                        },
                        enabled = !errors.any { isError -> isError() }
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
    onValueUnitChange: (index: Int, newValue: Double) -> Unit,
) {
    itemsIndexed(payments) { index, payment ->
        var expanded by rememberSaveable { mutableStateOf(false) }
        val textValue = if (payment.amountUnit == 0.0) {
            ""
        } else {
            if (payment.unit == PaymentUnit.Percentage)
                (payment.amountUnit * 100).toString()
            else
                payment.amountUnit.toString()
        }

        val unitError = { payment.amountUnit < 0 }
        val valueError = { payment.amountMoney < 0 }
        val errors = listOf(unitError, valueError)
        val error = errors.any { isError -> isError() }

        Box {
            OutlinedTextField(
                modifier = Modifier
                    .animateContentSize()
                    .fillMaxWidth(),
                maxLines = 1,
                value = textValue,
                onValueChange = { newValue ->
                    onValueUnitChange(
                        index,
                        (newValue.toDoubleOrNull() ?: 0.0)
                            .let { if (payment.unit == PaymentUnit.Percentage) it / 100.0 else it })
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
fun FormPreview() {
    NewExpenseForm(onFinish = {}, modifier = Modifier.fillMaxSize())
}

@Preview(showBackground = true)
@Composable
fun UserListPreview() {
    val payments = rememberSaveable {
        mutableStateListOf(*(0..5).map {
            UserPaymentState(UserModel.default(), PaymentUnit.Additive, 0.0, 0.0)
        }.toTypedArray())
    }

    LazyColumn {
        divisionListForm(
            payments = payments,
            onUpdateUnit = { _, _ -> },
            onValueUnitChange = { _, _ -> })
    }
}