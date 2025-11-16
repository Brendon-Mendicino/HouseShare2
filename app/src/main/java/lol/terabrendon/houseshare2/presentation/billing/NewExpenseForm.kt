package lol.terabrendon.houseshare2.presentation.billing

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType.Companion.PrimaryNotEditable
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import lol.terabrendon.houseshare2.R
import lol.terabrendon.houseshare2.domain.model.ExpenseCategory
import lol.terabrendon.houseshare2.domain.model.Money
import lol.terabrendon.houseshare2.domain.model.UserModel
import lol.terabrendon.houseshare2.presentation.components.AvatarIcon
import lol.terabrendon.houseshare2.presentation.components.FormOutlinedTextField
import lol.terabrendon.houseshare2.presentation.components.RegisterBackNavIcon
import lol.terabrendon.houseshare2.presentation.navigation.HomepageNavigation
import lol.terabrendon.houseshare2.presentation.provider.FabConfig
import lol.terabrendon.houseshare2.presentation.provider.RegisterFabConfig
import lol.terabrendon.houseshare2.presentation.util.SnackbarController
import lol.terabrendon.houseshare2.presentation.util.SnackbarEvent
import lol.terabrendon.houseshare2.presentation.util.errorText
import lol.terabrendon.houseshare2.presentation.vm.NewExpenseFormViewModel
import lol.terabrendon.houseshare2.presentation.vm.NewExpenseFormViewModel.UiEvent
import lol.terabrendon.houseshare2.util.ObserveAsEvent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewExpenseForm(
    modifier: Modifier = Modifier,
    viewModel: NewExpenseFormViewModel = hiltViewModel(),
    onFinish: () -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val expenseFormState by viewModel.expenseFormState.collectAsStateWithLifecycle()
    val users by viewModel.users.collectAsStateWithLifecycle()
    val userSelected by viewModel.userSelected.collectAsStateWithLifecycle()
    val simpleDivisionParts by viewModel.simpleDivisionParts.collectAsStateWithLifecycle()

    ObserveAsEvent(viewModel.eventChannelFlow) { event ->
        when (event) {
            is UiEvent.Error -> scope.launch {
                SnackbarController.sendEvent(
                    SnackbarEvent(
                        message = event.error.errorText(
                            event.label,
                            context
                        )
                    )
                )
            }

            is UiEvent.Finish -> onFinish()
        }
    }

    RegisterFabConfig<HomepageNavigation.ExpenseForm>(
        config = FabConfig.Fab(
            visible = true,
            expanded = false,
            icon = { Icon(Icons.Default.Check, null) },
            onClick = { viewModel.onEvent(ExpenseFormEvent.Submit) }
        )
    )

    RegisterBackNavIcon<HomepageNavigation.ExpenseForm>(onClick = onFinish)

    NewExpenseFormInner(
        modifier = modifier,
        state = expenseFormState,
        users = users,
        userSelected = userSelected,
        simpleDivisionParts = simpleDivisionParts,
        onEvent = viewModel::onEvent,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewExpenseFormInner(
    modifier: Modifier = Modifier,
    state: ExpenseFormStateValidator,
    users: List<UserModel>,
    userSelected: List<Boolean>,
    simpleDivisionParts: List<Money>,
    onEvent: (ExpenseFormEvent) -> Unit = {},
) {
    var categoryExpended by remember { mutableStateOf(false) }
    var payerExpanded by remember { mutableStateOf(false) }
    val simpleDivision = state.simpleDivisionEnabled.value

    Column(
        modifier
            .padding(8.dp)
            .verticalScroll(rememberScrollState())
            .animateContentSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FormOutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            param = state.totalAmount,
            supportParams = listOf(state.totalAmountMoney),
            onValueChange = { onEvent(ExpenseFormEvent.TotalAmountChanged(it)) },
            labelText = stringResource(R.string.amount),
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Next,
                keyboardType = KeyboardType.Decimal,
            ),
        )

        FormOutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            param = state.title,
            onValueChange = { onEvent(ExpenseFormEvent.TitleChanged(it)) },
            labelText = stringResource(R.string.title),
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Next,
            ),
        )

        ExposedDropdownMenuBox(
            expanded = categoryExpended,
            onExpandedChange = { categoryExpended = it },
        ) {
            val category = state.category.value

            FormOutlinedTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .animateContentSize()
                    .menuAnchor(PrimaryNotEditable),
                readOnly = true,
                param = state.category,
                value = category?.let { stringResource(it.toStringRes()) } ?: "",
                onValueChange = {},
                labelText = stringResource(R.string.category),
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
                            categoryExpended = false
                            onEvent(ExpenseFormEvent.CategoryToggled(entry))
                        },
                        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                    )
                }
            }
        }

        ExposedDropdownMenuBox(
            expanded = payerExpanded,
            onExpandedChange = { payerExpanded = it }
        ) {
            FormOutlinedTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .animateContentSize()
                    .menuAnchor(PrimaryNotEditable),
                readOnly = true,
                param = state.payer,
                value = state.payer.value?.username ?: "",
                onValueChange = {},
                labelText = stringResource(R.string.payed_by),
                leadingIcon = state.payer.value?.let { { AvatarIcon(user = it, size = 24.dp) } },
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
                        leadingIcon = { AvatarIcon(user = user, size = 24.dp) },
                        onClick = {
                            onEvent(ExpenseFormEvent.PayerChanged(user))
                            payerExpanded = false
                        },
                        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                    )
                }
            }
        }


        FormOutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            param = state.description,
            onValueChange = { onEvent(ExpenseFormEvent.DescriptionChanged(it)) },
            labelText = stringResource(R.string.description),
            minLines = 3,
            maxLines = 5,
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Next,
            ),
        )

        // List of contributors
        HorizontalDivider(Modifier.padding(8.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Switch(
                checked = !simpleDivision,
                onCheckedChange = {
                    onEvent(ExpenseFormEvent.SimpleDivisionToggled)
                },
                thumbContent = {
                    AnimatedVisibility(!simpleDivision) {
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = null,
                            modifier = Modifier.size(SwitchDefaults.IconSize),
                        )
                    }
                },
            )

            Text(stringResource(R.string.advanced_division))
        }

        // Draw the list of user parts
        AnimatedContent(simpleDivision) { simpleDivision ->
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (simpleDivision) {
                    simpleDivisionParts.zip(userSelected).zip(users)
                        .forEachIndexed { index, (pair, user) ->
                            val (money, selected) = pair

                            SimplePartItem(
                                selected = selected,
                                money = money,
                                username = user.username,
                                onToggle = {
                                    onEvent(ExpenseFormEvent.SimpleDivisionUserToggled(index))
                                },
                            )
                        }
                } else {
                    state.userParts.value.zip(state.convertedValues.value).zip(users)
                        .forEachIndexed { index, (pair, user) ->
                            val (part, converted) = pair
                            UserPartField(
                                part = part.toValidator(),
                                user = user,
                                convertedAmount = converted,
                                onUnitChanged = {
                                    onEvent(ExpenseFormEvent.UnitChanged(index, it))
                                },
                                onAmountChanged = {
                                    onEvent(ExpenseFormEvent.UserPartChanged(index, it))
                                },
                            )
                        }
                }
            }
        }

        Spacer(Modifier.requiredHeight(100.dp))
    }
}

@Composable
private fun SimplePartItem(
    modifier: Modifier = Modifier,
    selected: Boolean,
    money: Money,
    username: String,
    onToggle: () -> Unit,
) {
    val color = if (selected) MaterialTheme.colorScheme.onSurface
    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)

    CompositionLocalProvider(
        LocalContentColor provides color
    ) {
        OutlinedTextField(
            modifier = modifier
                .animateContentSize()
                .fillMaxWidth(),
            readOnly = true,
            value = username,
            onValueChange = {},
            leadingIcon = {
                Checkbox(
                    checked = selected,
                    onCheckedChange = { onToggle() }
                )
            },
            suffix = {
                Text(money.toCurrency())
            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UserPartField(
    part: UserPartValidator,
    user: UserModel,
    convertedAmount: Money,
    onUnitChanged: (unit: PaymentUnit) -> Unit,
    onAmountChanged: (amount: String) -> Unit,
) {
    var expanded by rememberSaveable { mutableStateOf(false) }

    Box {
        FormOutlinedTextField(
            modifier = Modifier
                .animateContentSize()
                .fillMaxWidth(),
            param = part.amount,
            labelText = user.username,
            onValueChange = { onAmountChanged(it) },
            suffix = {
                Text(convertedAmount.toCurrency())
            },
            leadingIcon = {
                IconButton(onClick = { expanded = !expanded }) {
                    Icon(part.paymentUnit.value.toImageVector(), contentDescription = null)
                }
            },
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
                        expanded = false
                        onUnitChanged(entry)
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                )
            }
        }
    }
}

@Preview(showSystemUi = true, showBackground = true)
@Composable
private fun FormPreview(
    state: ExpenseFormState = ExpenseFormState(),
) {
    NewExpenseFormInner(
        state = state.toValidator(),
        users = emptyList(),
        userSelected = emptyList(),
        simpleDivisionParts = emptyList()
    )
}

@Preview(showBackground = true)
@Composable
private fun UserListPreview() {
//    val payments = rememberSaveable {
//        mutableStateListOf(*(0..5).map {
//            UserPaymentState.default()
//        }.toTypedArray())
//    }
//
//    DivisionListForm(
//        payments = listOf(
//            UserPaymentState.default().copy(amountMoney = 123.44, amountUnit = "123.44")
//        ) + payments,
//        onUpdateUnit = { _, _ -> },
//        onValueUnitChange = { _, _ -> })
}

@Preview(showBackground = true)
@Composable
private fun SimplePartListPreview() {
//    val payments = rememberSaveable {
//        mutableStateListOf(*(0..5).map {
//            UserPaymentState.default()
//        }.toTypedArray())
//    }
//
//    HouseShare2Theme {
//        SimplePartList(
//            payments = listOf(
//                UserPaymentState.default().copy(amountMoney = 123.44, amountUnit = "123.44")
//            ) + payments,
//            onToggle = {},
//            selected = listOf(true, true, false, false, false)
//        )
//    }
}
