package lol.terabrendon.houseshare2.domain.mapper

import com.google.common.truth.Truth.assertThat
import lol.terabrendon.houseshare2.domain.model.BillingBalanceModel
import lol.terabrendon.houseshare2.domain.model.DebtModel
import lol.terabrendon.houseshare2.domain.model.ExpenseModel
import lol.terabrendon.houseshare2.domain.model.GroupMemberModel
import lol.terabrendon.houseshare2.domain.model.Money
import lol.terabrendon.houseshare2.domain.model.UserExpenseModel
import lol.terabrendon.houseshare2.domain.model.toMoney
import org.junit.Test

class DebtSettlementMapperTest {

    private val mapper = DebtSettlementMapper()

    private fun member(id: Long, name: String) =
        GroupMemberModel.random().copy(id = id, firstName = name)

    private val alice = member(1, "Alice")
    private val bob = member(2, "Bob")
    private val carol = member(3, "Carol")
    private val dave = member(4, "Dave")

    private fun balance(member: GroupMemberModel, amount: String) =
        BillingBalanceModel(member, amount.toMoney())

    /**
     * Applying every debt to the balances must bring all of them back to zero.
     */
    private fun assertSettles(balances: Collection<BillingBalanceModel>, debts: List<DebtModel>) {
        val net = balances.associate { it.user.id to it.finalBalance.compact }.toMutableMap()
        debts.forEach { debt ->
            net[debt.debtor.id] = net.getValue(debt.debtor.id) + debt.amount.compact
            net[debt.creditor.id] = net.getValue(debt.creditor.id) - debt.amount.compact
        }
        assertThat(net.values.toSet()).containsExactly(0L)
    }

    @Test
    fun `map with no balances should return no debts`() {
        assertThat(mapper.map(emptyList())).isEmpty()
    }

    @Test
    fun `map with all zero balances should return no debts`() {
        val balances = listOf(balance(alice, "0"), balance(bob, "0"))

        assertThat(mapper.map(balances)).isEmpty()
    }

    @Test
    fun `map with one creditor should make everyone pay the creditor`() {
        val balances = listOf(
            balance(alice, "1333.70"),
            balance(bob, "-414.71"),
            balance(carol, "-467.90"),
            balance(dave, "-451.09"),
        )

        val debts = mapper.map(balances)

        // Biggest debtor first.
        assertThat(debts).containsExactly(
            DebtModel(debtor = carol, creditor = alice, amount = "467.90".toMoney()),
            DebtModel(debtor = dave, creditor = alice, amount = "451.09".toMoney()),
            DebtModel(debtor = bob, creditor = alice, amount = "414.71".toMoney()),
        ).inOrder()
        assertSettles(balances, debts)
    }

    @Test
    fun `map with many creditors and debtors should settle everyone in at most n-1 payments`() {
        val balances = listOf(
            balance(alice, "70.00"),
            balance(bob, "30.50"),
            balance(carol, "-45.25"),
            balance(dave, "-55.25"),
        )

        val debts = mapper.map(balances)

        assertThat(debts.size).isAtMost(balances.size - 1)
        debts.forEach { debt ->
            assertThat(debt.amount).isGreaterThan(Money.ZERO)
            assertThat(debt.debtor.id).isNotEqualTo(debt.creditor.id)
        }
        assertSettles(balances, debts)
    }

    @Test
    fun `map with equal amounts should be deterministic`() {
        val balances = listOf(
            balance(bob, "10.00"),
            balance(alice, "10.00"),
            balance(dave, "-10.00"),
            balance(carol, "-10.00"),
        )

        val debts = mapper.map(balances)

        // Ties are broken by member id.
        assertThat(debts).containsExactly(
            DebtModel(debtor = carol, creditor = alice, amount = "10.00".toMoney()),
            DebtModel(debtor = dave, creditor = bob, amount = "10.00".toMoney()),
        ).inOrder()
        assertThat(mapper.map(balances.reversed())).isEqualTo(debts)
    }

    @Test
    fun `map of the balances of real expenses should settle the group`() {
        val expenses = listOf(
            // Alice pays the rent for everyone.
            ExpenseModel.default().copy(
                id = 1,
                expensePayer = alice,
                userExpenses = listOf(alice, bob, carol, dave)
                    .map { UserExpenseModel(it, "450.00".toMoney()) },
            ),
            // Bob pays the internet for everyone, unevenly.
            ExpenseModel.default().copy(
                id = 2,
                expensePayer = bob,
                userExpenses = listOf(
                    UserExpenseModel(alice, "7.50".toMoney()),
                    UserExpenseModel(bob, "7.50".toMoney()),
                    UserExpenseModel(carol, "7.50".toMoney()),
                    UserExpenseModel(dave, "7.49".toMoney()),
                ),
            ),
            // Carol pays a pizza for Bob and Dave only.
            ExpenseModel.default().copy(
                id = 3,
                expensePayer = carol,
                userExpenses = listOf(
                    UserExpenseModel(bob, "12.00".toMoney()),
                    UserExpenseModel(dave, "12.00".toMoney()),
                ),
            ),
        )
        val balances = ExpenseBalanceMapper().map(expenses).values

        val debts = mapper.map(balances)

        assertThat(debts).isNotEmpty()
        assertThat(debts.map { it.creditor }).contains(alice)
        assertSettles(balances, debts)
    }
}
