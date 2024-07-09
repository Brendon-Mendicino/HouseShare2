package lol.terabrendon.houseshare2.presentation.vm

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import lol.terabrendon.houseshare2.model.ExpenseCategory
import lol.terabrendon.houseshare2.model.ExpenseModel
import lol.terabrendon.houseshare2.model.UserExpenseModel
import lol.terabrendon.houseshare2.model.UserModel
import lol.terabrendon.houseshare2.presentation.billing.PaymentUnit
import lol.terabrendon.houseshare2.presentation.billing.UserPaymentState
import lol.terabrendon.houseshare2.repository.ExpenseRepository
import lol.terabrendon.houseshare2.repository.UserRepository
import lol.terabrendon.houseshare2.util.combineState
import java.time.LocalDateTime
import javax.inject.Inject

@HiltViewModel
class NewExpenseFormViewModel @Inject constructor(
    private val expenseRepository: ExpenseRepository,
    private val userRepository: UserRepository,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {
    companion object {
        private const val TAG = "NewExpenseFormViewModel"
    }

    private fun updatePaymentUnits(size: Int) {
        paymentUnits.value = (0..size).map { PaymentUnit.Additive }
    }

    private fun updatePaymentValueUnits(size: Int) {
        paymentValueUnits.value = (0..size).map { 0.0 }
    }

    private val _isFinished = MutableStateFlow(false)

    /**
     * This value is used to terminate the screen when all the operations
     * are finished
     */
    val isFinished: StateFlow<Boolean> = _isFinished


    // TODO: use real users of the group
    private val users = userRepository.findAll()
        .onEach {
            Log.i(TAG, "Getting updated list of users from the database.")
            updatePaymentUnits(it.size)
            updatePaymentValueUnits(it.size)
        }
        .stateIn(
            viewModelScope, SharingStarted.WhileSubscribed(5000), listOf()
        )

    private var _moneyAmount = MutableStateFlow(0.0)
    val moneyAmount: StateFlow<Double> = _moneyAmount

    private var _description = MutableStateFlow<String?>(null)
    val description: StateFlow<String?> = _description

    private var _title = MutableStateFlow("")
    val title: StateFlow<String> = _title

    private var _category = MutableStateFlow<ExpenseCategory?>(null)
    val category: StateFlow<ExpenseCategory?> = _category

    // TODO: add current user as default
    private var _payer = MutableStateFlow<UserModel?>(null)
    val payer: StateFlow<UserModel?> = _payer

    private var paymentUnits = MutableStateFlow(listOf<PaymentUnit>())

    private var paymentValueUnits = MutableStateFlow(listOf<Double>())

    private var _payments =
        combineState(
            viewModelScope,
            _moneyAmount,
            users,
            paymentUnits,
            paymentValueUnits,
        ) { totalMoney, users, paymentUnits, paymentValueUnits ->
            val nonQuotaMoney = paymentUnits.zip(paymentValueUnits).sumOf { (unit, value) ->
                when (unit) {
                    PaymentUnit.Additive -> value
                    PaymentUnit.Percentage -> totalMoney * value
                    PaymentUnit.Quota -> 0.0
                }
            }

            val totalQuotes = paymentUnits.zip(paymentValueUnits)
                .sumOf { (unit, value) -> if (unit == PaymentUnit.Quota) value else 0.0 }

            users.zip(paymentUnits).zip(paymentValueUnits).map { (pair, amountUnit) ->
                val (user, unit) = pair
                val moneyPerQuota =
                    if (totalQuotes != 0.0) (totalMoney - nonQuotaMoney) * amountUnit / totalQuotes else 0.0

                when (unit) {
                    PaymentUnit.Additive -> UserPaymentState(
                        user = user,
                        unit = unit,
                        amountUnit = amountUnit,
                        amountMoney = amountUnit,
                    )

                    PaymentUnit.Percentage -> UserPaymentState(
                        user = user,
                        unit = unit,
                        amountUnit = amountUnit,
                        amountMoney = amountUnit * totalMoney,
                    )

                    PaymentUnit.Quota -> UserPaymentState(
                        user = user,
                        unit = unit,
                        amountUnit = amountUnit,
                        amountMoney = moneyPerQuota,
                    )

                }
            }
        }
    val payments: StateFlow<List<UserPaymentState>> = _payments

    fun onMoneyAmountChange(money: Double) {
        _moneyAmount.value = money
    }

    fun onTitleChange(title: String) {
        _title.value = title
    }

    fun onDescriptionChange(description: String?) {
        _description.value = description
    }

    fun onCategoryChange(category: ExpenseCategory?) {
        _category.value = category
    }

    fun onUnitChange(index: Int, newUnit: PaymentUnit) {
        paymentUnits.update { units ->
            Log.i(TAG, "Updating paymentUnits with index $index")
            units.toMutableList().apply {
                this[index] = newUnit
            }
        }
    }

    fun onValueUnitChange(index: Int, newValue: Double) {
        paymentValueUnits.update { values ->
            Log.i(TAG, "Updating paymentValueUnits with index $index")
            values.toMutableList().apply {
                this[index] = newValue
            }
        }
    }

    fun onConfirm() {
        val expense = ExpenseModel(
            id = 0,
            amount = moneyAmount.value,
            // TODO: modify when we'll the current user
            expenseOwner = users.value.first(),
            expensePayer = payer.value ?: return,
            category = category.value ?: return,
            title = title.value,
            description = description.value,
            creationTimestamp = LocalDateTime.now(),
            userExpenses = payments.value.map {
                UserExpenseModel(
                    user = it.user,
                    partAmount = it.amountMoney,
                )
            },
        )

        viewModelScope.launch {
            Log.i(TAG, "Inserting a new expense with title \"${expense.title}\" to the repository")
            expenseRepository.insert(expense)
            _isFinished.value = true
        }
    }
}