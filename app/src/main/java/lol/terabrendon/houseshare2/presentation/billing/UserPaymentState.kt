package lol.terabrendon.houseshare2.presentation.billing

import lol.terabrendon.houseshare2.domain.model.UserModel

// TODO: make this a @FormState
data class UserPaymentState(
    val user: UserModel,
    val unit: PaymentUnit,
    val amountUnit: String,
    val amountMoney: Double,
) {
    companion object {
        fun default() = UserPaymentState(
            user = UserModel.default(),
            unit = PaymentUnit.Additive,
            amountUnit = "",
            amountMoney = 0.0,
        )
    }
}
