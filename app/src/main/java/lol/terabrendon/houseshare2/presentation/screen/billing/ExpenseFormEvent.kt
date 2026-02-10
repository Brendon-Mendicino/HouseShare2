package lol.terabrendon.houseshare2.presentation.screen.billing

import lol.terabrendon.houseshare2.domain.model.ExpenseCategory
import lol.terabrendon.houseshare2.domain.model.UserModel

sealed class ExpenseFormEvent {
    data class TotalAmountChanged(val amount: String) : ExpenseFormEvent()
    data class TitleChanged(val title: String) : ExpenseFormEvent()
    data class DescriptionChanged(val description: String) : ExpenseFormEvent()
    data class CategoryToggled(val category: ExpenseCategory) : ExpenseFormEvent()
    data class PayerChanged(val payer: UserModel) : ExpenseFormEvent()
    data class UnitChanged(val index: Int, val newUnit: PaymentUnit) : ExpenseFormEvent()
    data class UserPartChanged(val index: Int, val newValue: String) : ExpenseFormEvent()
    data class SimpleDivisionUserToggled(val index: Int) : ExpenseFormEvent()
    data object SimpleDivisionToggled : ExpenseFormEvent()
    object Submit : ExpenseFormEvent()
}
