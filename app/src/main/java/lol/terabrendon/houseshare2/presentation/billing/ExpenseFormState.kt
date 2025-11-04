package lol.terabrendon.houseshare2.presentation.billing

import lol.terabrendon.houseshare2.domain.model.ExpenseCategory
import lol.terabrendon.houseshare2.domain.model.UserModel

// TODO: make this a @FormState
data class ExpenseFormState(
    val moneyAmount: Double = 0.0,
    val description: String? = null,
    val title: String = "",
    val category: ExpenseCategory? = null,
    val payer: UserModel? = null,
    val paymentUnits: List<PaymentUnit> = emptyList(),
    val paymentValueUnits: List<String?> = emptyList(),
)
