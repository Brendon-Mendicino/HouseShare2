package lol.terabrendon.houseshare2.domain.model

/**
 * A single payment that settles part of the group balance: [debtor] has to give [amount] to
 * [creditor].
 */
data class DebtModel(
    val debtor: GroupMemberModel,
    val creditor: GroupMemberModel,
    /**
     * Always greater than zero.
     */
    val amount: Money,
) {
    companion object {
        @JvmStatic
        fun default() = DebtModel(
            debtor = GroupMemberModel.default(),
            creditor = GroupMemberModel.default(),
            amount = 0.toMoney(),
        )
    }
}
