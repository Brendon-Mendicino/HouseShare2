package lol.terabrendon.houseshare2.presentation.vm

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import lol.terabrendon.houseshare2.model.ExpenseCategory
import lol.terabrendon.houseshare2.model.UserModel
import lol.terabrendon.houseshare2.presentation.billing.PaymentUnit
import lol.terabrendon.houseshare2.presentation.billing.UserPaymentState
import lol.terabrendon.houseshare2.util.combineState
import javax.inject.Inject

@HiltViewModel
class BillingFormViewModel @Inject constructor() : ViewModel() {
    companion object {
        private const val TAG = "BillingFormViewModel"
    }

    private val users = MutableStateFlow(
        listOf(
            UserModel(0, "Brendon"),
            UserModel(1, "Flavy"),
            UserModel(2, "Cipolla"),
        )
    )

    private var _moneyAmount = MutableStateFlow(0.0)
    val moneyAmount: StateFlow<Double> = _moneyAmount

    private var _description = MutableStateFlow<String?>(null)
    val description: StateFlow<String?> = _description

    private var _title = MutableStateFlow("")
    val title: StateFlow<String> = _title

    private var _category = MutableStateFlow<ExpenseCategory?>(null)
    val category: StateFlow<ExpenseCategory?> = _category

    private var paymentUnits = MutableStateFlow((0..users.value.size).map { PaymentUnit.Additive })
    private var paymentValueUnits = MutableStateFlow((0..users.value.size).map { 0.0 })

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
}