package lol.terabrendon.houseshare2.domain.model

data class BillingBalanceModel(
    val user: GroupMemberModel,
    val finalBalance: Money,
) {
    companion object {
        fun default() = BillingBalanceModel(
            user = GroupMemberModel.default(),
            finalBalance = 0.toMoney(),
        )
    }
}