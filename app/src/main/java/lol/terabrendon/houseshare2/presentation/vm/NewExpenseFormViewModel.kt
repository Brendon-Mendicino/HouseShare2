package lol.terabrendon.houseshare2.presentation.vm

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.michaelbull.result.getOrElse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import lol.terabrendon.houseshare2.data.repository.ExpenseRepository
import lol.terabrendon.houseshare2.domain.mapper.ExpenseModelMapper
import lol.terabrendon.houseshare2.domain.usecase.GetLoggedUserUseCase
import lol.terabrendon.houseshare2.domain.usecase.GetSelectedGroupUseCase
import lol.terabrendon.houseshare2.presentation.billing.ExpenseFormEvent
import lol.terabrendon.houseshare2.presentation.billing.ExpenseFormState
import lol.terabrendon.houseshare2.presentation.billing.PaymentUnit
import lol.terabrendon.houseshare2.presentation.billing.UserPaymentState
import lol.terabrendon.houseshare2.util.CombinedStateFlow
import lol.terabrendon.houseshare2.util.combineState
import javax.inject.Inject

@HiltViewModel
class NewExpenseFormViewModel @Inject constructor(
    private val expenseRepository: ExpenseRepository,
    getSelectedGroup: GetSelectedGroupUseCase,
    getLoggedUserUseCase: GetLoggedUserUseCase,
) : ViewModel() {
    companion object {
        private const val TAG = "NewExpenseFormViewModel"
    }

    private val expenseModelMapper: ExpenseModelMapper = ExpenseModelMapper()

    private val finishedChannel = Channel<Unit>()

    /**
     * This channel is used to terminate the screen when all the operations
     * are finished
     */
    val finishedChannelFlow: Flow<Unit> = finishedChannel.receiveAsFlow()

    private val loggedUser = getLoggedUserUseCase.execute()
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private val selectedGroup = getSelectedGroup.execute()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)


    val users = selectedGroup
        .map { it?.users ?: emptyList() }
        .onEach {
            Log.i(TAG, "onEach: Getting updated list of users from the database.")
        }
        .stateIn(
            viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
        )

    private var _expenseFormState =
        CombinedStateFlow(ExpenseFormState(), viewModelScope, users) { formState, users ->
            // Keep the formState lists updated in case their sizes differs from
            // the number of users.
            val size = users.size

            if (size == formState.paymentUnits.size)
                return@CombinedStateFlow formState

            Log.i(
                TAG,
                "CombinedStateFlow: updating _expenseFormSate lists because their sizes differ from the one the user! users.size=$size, formState.paymentUnits.size=${formState.paymentUnits.size}"
            )

            formState.copy(
                paymentUnits = (0..<size).map { PaymentUnit.Additive },
                paymentValueUnits = (0..<size).map { null },
            )
        }

    val expenseFormState = _expenseFormState.asStateFlow()

    val payments =
        combineState(
            viewModelScope,
            users,
            expenseFormState,
        ) { users, formState ->
            val totalMoney = formState.moneyAmount
            val paymentUnits = formState.paymentUnits
            val paymentValueUnits = formState.paymentValueUnits.map { it?.toDoubleOrNull() ?: 0.0 }

            val nonQuotaMoney = paymentUnits
                .zip(paymentValueUnits)
                .sumOf { (unit, value) ->
                    when (unit) {
                        PaymentUnit.Additive -> value
                        PaymentUnit.Percentage -> totalMoney * (value / 100.0)
                        PaymentUnit.Quota -> 0.0
                    }
                }

            val totalQuotes = paymentUnits.zip(paymentValueUnits)
                .sumOf { (unit, value) -> if (unit == PaymentUnit.Quota) value else 0.0 }

            users
                .zip(paymentUnits)
                .zip(paymentValueUnits)
                .map { (pair, amountUnit) ->
                    val (user, unit) = pair
                    val moneyPerQuota =
                        if (totalQuotes != 0.0) (totalMoney - nonQuotaMoney) * amountUnit / totalQuotes else 0.0

                    when (unit) {
                        PaymentUnit.Additive -> UserPaymentState(
                            user = user,
                            unit = unit,
                            amountUnit = "",
                            amountMoney = amountUnit,
                        )

                        PaymentUnit.Percentage -> UserPaymentState(
                            user = user,
                            unit = unit,
                            amountUnit = "",
                            amountMoney = (amountUnit / 100.0) * totalMoney,
                        )

                        PaymentUnit.Quota -> UserPaymentState(
                            user = user,
                            unit = unit,
                            amountUnit = "",
                            amountMoney = moneyPerQuota,
                        )

                    }
                }
                // Fill the payments with the current string values in the form
                .zip(formState.paymentValueUnits)
                .map { (user, amountUnit) ->
                    user.copy(amountUnit = amountUnit ?: "")
                }
        }

    fun onEvent(event: ExpenseFormEvent) {
        when (event) {
            is ExpenseFormEvent.CategoryChanged -> {
                _expenseFormState.update { it.copy(category = event.category) }
            }

            is ExpenseFormEvent.DescriptionChanged -> {
                _expenseFormState.update { it.copy(description = event.description) }
            }

            is ExpenseFormEvent.MoneyAmountChanged -> {
                _expenseFormState.update { it.copy(moneyAmount = event.money) }
            }

            is ExpenseFormEvent.PayerChanged -> {
                _expenseFormState.update { it.copy(payer = event.payer) }
            }

            is ExpenseFormEvent.TitleChanged -> {
                _expenseFormState.update { it.copy(title = event.title) }
            }

            is ExpenseFormEvent.UnitChanged -> {
                Log.i(TAG, "Updating paymentUnits with index ${event.index}")
                _expenseFormState.update {
                    it.copy(
                        paymentUnits = it.paymentUnits.toMutableList().apply {
                            this[event.index] = event.newUnit
                        },
                    )
                }
            }

            is ExpenseFormEvent.ValueUnitChanged -> {
                Log.i(TAG, "Updating paymentValueUnits with index ${event.index}")
                _expenseFormState.update {
                    it.copy(
                        paymentValueUnits = it.paymentValueUnits.toMutableList().apply {
                            this[event.index] = event.newValue
                        },
                    )
                }
            }

            ExpenseFormEvent.Submit -> onConfirm()
        }
    }

    private fun onConfirm() {
        // The owner of the expense if the current logged user.
        // TODO: refactor some failure event of some sort...
        val owner = loggedUser.value ?: throw IllegalStateException("No logged users!")

        val groupId = selectedGroup.value?.info?.groupId
            ?: run {
                val msg =
                    "There is not selectedGroup! Choose a group first and then proceed with the form!"
                Log.e(TAG, "onConfirm: $msg")
                throw IllegalStateException(msg)
            }

        val expense = expenseModelMapper
            .map(
                formState = expenseFormState.value,
                expenseOwner = owner,
                payments = payments.value,
                groupId = groupId,
            )
            .getOrElse {
                val msg = "expense model mapping failed! Error msg: $it"
                Log.e(TAG, "onConfirm: $msg")
                throw IllegalStateException(msg)
            }

        viewModelScope.launch {
            Log.i(TAG, "Inserting a new expense with title \"${expense.title}\" to the repository")

            expenseRepository.insert(expense)

            finishedChannel.send(Unit)
        }
    }
}