package lol.terabrendon.houseshare2.presentation.billing

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import lol.terabrendon.houseshare2.R
import lol.terabrendon.houseshare2.model.ExpenseCategory
import lol.terabrendon.houseshare2.model.ExpenseModel
import lol.terabrendon.houseshare2.model.UserModel
import lol.terabrendon.houseshare2.presentation.vm.BillingFormViewModel
import lol.terabrendon.houseshare2.util.currencyFormat
import java.time.LocalDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BillingForm(
    modifier: Modifier = Modifier,
    billingFormViewModel: BillingFormViewModel = hiltViewModel(),
    onFinish: (ExpenseModel) -> Unit,
    onBack: () -> Unit
) {
    var categoryExpended by remember { mutableStateOf(false) }

    val moneyAmount by billingFormViewModel.moneyAmount.collectAsStateWithLifecycle()
    val description by billingFormViewModel.description.collectAsStateWithLifecycle()
    val title by billingFormViewModel.title.collectAsStateWithLifecycle()
    val category by billingFormViewModel.category.collectAsStateWithLifecycle()
    val payments by billingFormViewModel.payments.collectAsStateWithLifecycle()

    val amountError = { moneyAmount <= 0.0 }
    val categoryError = { category == null }
    val titleError = { title.isEmpty() }
    val errors = listOf(amountError, categoryError, titleError)


    LazyColumn(
        modifier.padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
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
        }

        item {
            FormTextField(
                value = if (moneyAmount != 0.0) moneyAmount.currencyFormat() else "",
                onValueChange = {
                    billingFormViewModel.onMoneyAmountChange(
                        it.currencyFormat() ?: 0.0
                    )
                },
                isError = amountError,
                label = { Text(stringResource(R.string.amount), maxLines = 1) },
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            Row(modifier = Modifier.fillMaxWidth()) {
                FormTextField(
                    value = title,
                    onValueChange = { billingFormViewModel.onTitleChange(it) },
                    label = { Text(stringResource(R.string.title), maxLines = 1) },
                    isError = titleError,
                    errorText = { Text(stringResource(R.string.title_should_not_be_empty)) },
                    maxLines = 1,
                    modifier = Modifier.weight(weight = 0.5f)
                )

                ExposedDropdownMenuBox(
                    expanded = categoryExpended,
                    onExpandedChange = { categoryExpended = it },
                    modifier = Modifier.weight(weight = 0.5f)
                ) {
                    OutlinedTextField(
                        modifier = Modifier.menuAnchor(),
                        readOnly = true,
                        maxLines = 1,
                        value = category?.let { stringResource(it.toStringRes()) } ?: "",
                        onValueChange = {},
                        leadingIcon = {
                            category?.let {
                                Icon(it.toImageVector(), contentDescription = null)
                            }
                        },
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
                                    billingFormViewModel.onCategoryChange(if (entry == category) null else entry)
                                    categoryExpended = false
                                },
                                contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                            )
                        }
                    }
                }
            }
        }

        item {
            FormTextField(
                value = description ?: "",
                onValueChange = { billingFormViewModel.onDescriptionChange(it) },
                label = {
                    Text(stringResource(R.string.description))
                },
                minLines = 3,
                maxLines = 10,
                modifier = Modifier.fillMaxWidth()
            )
        }


        DivisionListForm(
            payments = payments,
            onUpdateUnit = billingFormViewModel::onUnitChange,
            onValueUnitChange = billingFormViewModel::onValueUnitChange
        )


        item {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                modifier = Modifier.fillMaxWidth()
            ) {
                TextButton(onClick = { onBack() }) {
                    Text(stringResource(R.string.dismiss))
                }

                TextButton(
                    onClick = {
                        onFinish(
                            ExpenseModel(
                                id = 0,
                                amount = moneyAmount,
                                title = title,
                                description = description,
                                category = category!!,
                                creationTimestamp = LocalDateTime.now(),
                                expenseOwner = UserModel(0, "Brendon"),
                                userExpenses = listOf()
                            )
                        )
                    },
                    enabled = !errors.any { isError -> isError() }
                ) {
                    Text(stringResource(R.string.confirm))
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
        supportingText = if (error) errorText else null,
        keyboardOptions = keyboardOptions,
        minLines = minLines,
        maxLines = maxLines,
        label = label,
        isError = error,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
private fun LazyListScope.DivisionListForm(
    payments: List<UserPaymentState>,
    onUpdateUnit: (index: Int, newUnit: PaymentUnit) -> Unit,
    onValueUnitChange: (index: Int, newValue: Double) -> Unit,
) {
    itemsIndexed(payments) { index, payment ->
        var expanded by rememberSaveable { mutableStateOf(false) }
        var paymentUnit by rememberSaveable { mutableStateOf(PaymentUnit.Additive) }
        val textValue = if (payment.amountUnit == 0.0) {
            ""
        } else {
            if (payment.unit == PaymentUnit.Percentage)
                (payment.amountUnit * 100).toString()
            else
                payment.amountUnit.toString()
        }

        Box {
            OutlinedTextField(
                modifier = Modifier
                    .padding(0.dp)
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
                        Icon(paymentUnit.toImageVector(), contentDescription = null)
                    }
                },
                suffix = {
                    Text(payment.amountMoney.currencyFormat())
                },
                label = { Text(payment.user.username) },
                trailingIcon = {
                    if (expanded)
                        Icon(imageVector = Icons.Filled.ArrowDropUp, contentDescription = null)
                    else
                        Icon(imageVector = Icons.Filled.ArrowDropDown, contentDescription = null)
                }
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
                            paymentUnit = entry
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
    BillingForm(onFinish = {}, onBack = {}, modifier = Modifier.fillMaxSize())
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
        DivisionListForm(
            payments = payments,
            onUpdateUnit = { _, _ -> },
            onValueUnitChange = { _, _ -> })
    }
}