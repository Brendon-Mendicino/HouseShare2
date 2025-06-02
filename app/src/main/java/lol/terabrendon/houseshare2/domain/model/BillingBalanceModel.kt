package lol.terabrendon.houseshare2.domain.model

data class BillingBalanceModel(
    val user: UserModel,
    val finalBalance: Double,
) {
    companion object {
        fun default() = BillingBalanceModel(
            user = UserModel.default(),
            finalBalance = 0.0,
        )
    }
}