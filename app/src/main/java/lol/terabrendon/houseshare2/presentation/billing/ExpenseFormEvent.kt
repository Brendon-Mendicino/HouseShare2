package lol.terabrendon.houseshare2.presentation.billing

import lol.terabrendon.houseshare2.model.ExpenseCategory
import lol.terabrendon.houseshare2.model.UserModel

sealed class ExpenseFormEvent {
    data class MoneyAmountChanged(val money: Double) : ExpenseFormEvent()
    data class TitleChanged(val title: String) : ExpenseFormEvent()
    data class DescriptionChanged(val description: String) : ExpenseFormEvent()
    data class CategoryChanged(val category: ExpenseCategory?) : ExpenseFormEvent()
    data class PayerChanged(val payer: UserModel) : ExpenseFormEvent()
    data class UnitChanged(val index: Int, val newUnit: PaymentUnit) : ExpenseFormEvent()
    data class ValueUnitChanged(val index: Int, val newValue: Double) : ExpenseFormEvent()
    object Submit : ExpenseFormEvent()
}
