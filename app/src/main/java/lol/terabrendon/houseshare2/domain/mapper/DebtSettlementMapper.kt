package lol.terabrendon.houseshare2.domain.mapper

import lol.terabrendon.houseshare2.domain.model.BillingBalanceModel
import lol.terabrendon.houseshare2.domain.model.DebtModel
import lol.terabrendon.houseshare2.domain.model.GroupMemberModel
import lol.terabrendon.houseshare2.domain.model.Money
import javax.inject.Inject
import kotlin.math.min

class DebtSettlementMapper @Inject constructor() {
    private data class Open(val member: GroupMemberModel, var cents: Long)

    /**
     * Map the final balances of a group (see [ExpenseBalanceMapper]) to the single payments that
     * settle it: who has to pay whom, and how much.
     *
     * The biggest debtor always pays the biggest creditor, so the group needs at most `n - 1`
     * payments. This means that a debt can point to a member that never paid anything for the
     * debtor directly: only the final balances matter.
     *
     * The balances must sum to zero, as the ones computed from the expenses do.
     */
    fun map(balances: Collection<BillingBalanceModel>): List<DebtModel> {
        // Work in cents: no scale or rounding issues while subtracting.
        fun open(sign: Int) = balances
            .map { Open(it.user, it.finalBalance.compact * sign) }
            .filter { it.cents > 0 }
            .sortedWith(compareByDescending<Open> { it.cents }.thenBy { it.member.id })

        val creditors = open(1)
        val debtors = open(-1)

        val debts = mutableListOf<DebtModel>()
        var c = 0
        var d = 0
        while (c < creditors.size && d < debtors.size) {
            val creditor = creditors[c]
            val debtor = debtors[d]
            val pay = min(creditor.cents, debtor.cents)

            debts += DebtModel(
                debtor = debtor.member,
                creditor = creditor.member,
                amount = Money.fromCompact(pay),
            )

            creditor.cents -= pay
            debtor.cents -= pay
            if (creditor.cents == 0L) c++
            if (debtor.cents == 0L) d++
        }

        return debts
    }
}
