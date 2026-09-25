package lol.terabrendon.houseshare2.domain.mapper

import lol.terabrendon.houseshare2.domain.model.ExpenseModel
import lol.terabrendon.houseshare2.domain.model.GroupMemberModel
import lol.terabrendon.houseshare2.domain.model.UserExpenseModel
import lol.terabrendon.houseshare2.domain.model.toMoney
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExpenseBalanceMapperTest {

    private val mapper = ExpenseBalanceMapper()

    @Test
    fun `map with empty list should return empty map`() {
        val result = mapper.map(emptyList())
        assertTrue(result.isEmpty())
    }

    @Test
    fun `map with single expense where payer is only user should return zero balance`() {
        val alice = GroupMemberModel.random().copy(id = 1L, firstName = "Alice")
        val expense = ExpenseModel.default().copy(
            id = 100L,
            amount = 10.toMoney(),
            expensePayer = alice,
            userExpenses = listOf(UserExpenseModel(alice, 10.toMoney()))
        )

        val result = mapper.map(listOf(expense))

        assertEquals("Map should contain exactly 1 user", 1, result.size)
        val balance = result[1L]
        assertNotNull("User Alice (ID 1) should be in the result", balance)
        assertEquals(0.toMoney(), balance?.finalBalance)
        assertEquals(alice, balance?.user)
    }

    @Test
    fun `map with single expense where payer is not in userExpenses should give full credit`() {
        val alice = GroupMemberModel.random().copy(id = 1L, firstName = "Alice")
        val bob = GroupMemberModel.random().copy(id = 2L, firstName = "Bob")

        val expense = ExpenseModel.default().copy(
            id = 100L,
            amount = 10.toMoney(),
            expensePayer = alice,
            userExpenses = listOf(UserExpenseModel(bob, 10.toMoney()))
        )

        val result = mapper.map(listOf(expense))

        assertEquals(2, result.size)
        assertEquals(10.toMoney(), result[1L]?.finalBalance) // Alice is owed 10
        assertEquals((-10).toMoney(), result[2L]?.finalBalance) // Bob owes 10
    }

    @Test
    fun `map with multiple expenses summing correctly`() {
        val alice = GroupMemberModel.random().copy(id = 1L, firstName = "Alice")
        val bob = GroupMemberModel.random().copy(id = 2L, firstName = "Bob")

        // 1. Alice pays 10, Bob owes 5
        val exp1 = ExpenseModel.default().copy(
            expensePayer = alice,
            userExpenses = listOf(
                UserExpenseModel(alice, 5.toMoney()),
                UserExpenseModel(bob, 5.toMoney())
            )
        )
        // 2. Bob pays 20, Alice owes 10
        val exp2 = ExpenseModel.default().copy(
            expensePayer = bob,
            userExpenses = listOf(
                UserExpenseModel(alice, 10.toMoney()),
                UserExpenseModel(bob, 10.toMoney())
            )
        )

        val result = mapper.map(listOf(exp1, exp2))

        // Alice: +5 - 10 = -5
        // Bob: -5 + 10 = +5
        assertEquals((-5).toMoney(), result[1L]?.finalBalance)
        assertEquals(5.toMoney(), result[2L]?.finalBalance)
    }

    @Test
    fun `map handles multiple beneficiaries in one expense`() {
        val payer = GroupMemberModel.random().copy(id = 1L)
        val u2 = GroupMemberModel.random().copy(id = 2L)
        val u3 = GroupMemberModel.random().copy(id = 3L)

        val expense = ExpenseModel.default().copy(
            expensePayer = payer,
            userExpenses = listOf(
                UserExpenseModel(payer, 10.toMoney()),
                UserExpenseModel(u2, 10.toMoney()),
                UserExpenseModel(u3, 10.toMoney())
            )
        )

        val result = mapper.map(listOf(expense))

        assertEquals(20.toMoney(), result[1L]?.finalBalance) // Owed 10+10
        assertEquals((-10).toMoney(), result[2L]?.finalBalance)
        assertEquals((-10).toMoney(), result[3L]?.finalBalance)
    }

    @Test
    fun `map handles complex circular debt resulting in zero`() {
        val a = GroupMemberModel.random().copy(id = 1L)
        val b = GroupMemberModel.random().copy(id = 2L)
        val c = GroupMemberModel.random().copy(id = 3L)

        val expenses = listOf(
            // A pays 10 for B
            ExpenseModel.default()
                .copy(expensePayer = a, userExpenses = listOf(UserExpenseModel(b, 10.toMoney()))),
            // B pays 10 for C
            ExpenseModel.default()
                .copy(expensePayer = b, userExpenses = listOf(UserExpenseModel(c, 10.toMoney()))),
            // C pays 10 for A
            ExpenseModel.default()
                .copy(expensePayer = c, userExpenses = listOf(UserExpenseModel(a, 10.toMoney())))
        )

        val result = mapper.map(expenses)

        assertEquals(0.toMoney(), result[1L]?.finalBalance)
        assertEquals(0.toMoney(), result[2L]?.finalBalance)
        assertEquals(0.toMoney(), result[3L]?.finalBalance)
    }

    @Test
    fun `map handles precision with minimal currency units`() {
        val alice = GroupMemberModel.random().copy(id = 1L)
        val bob = GroupMemberModel.random().copy(id = 2L)

        // Alice pays 0.01, split is 100% for Bob
        val expense = ExpenseModel.default().copy(
            expensePayer = alice,
            userExpenses = listOf(UserExpenseModel(bob, 0.01.toMoney()))
        )

        val result = mapper.map(listOf(expense))

        assertEquals(0.01.toMoney(), result[1L]?.finalBalance)
        assertEquals((-0.01).toMoney(), result[2L]?.finalBalance)
    }

    @Test
    fun `sum of all balances should always be zero`() {
        val a = GroupMemberModel.random().copy(id = 1L)
        val b = GroupMemberModel.random().copy(id = 2L)
        val c = GroupMemberModel.random().copy(id = 3L)

        val expenses = listOf(
            ExpenseModel.default().copy(
                expensePayer = a,
                userExpenses = listOf(
                    UserExpenseModel(a, 30.toMoney()),
                    UserExpenseModel(b, 20.toMoney()),
                    UserExpenseModel(c, 50.toMoney())
                )
            ),
            ExpenseModel.default().copy(
                expensePayer = b,
                userExpenses = listOf(
                    UserExpenseModel(a, 10.toMoney()),
                    UserExpenseModel(c, 40.toMoney())
                )
            )
        )

        val result = mapper.map(expenses)

        val total = result.values
            .map { it.finalBalance }
            .reduce { acc, money -> acc + money }

        assertEquals(0.toMoney(), total)
    }

    @Test
    fun `duplicate user entries in same expense should aggregate correctly`() {
        val payer = GroupMemberModel.random().copy(id = 1L)
        val bob = GroupMemberModel.random().copy(id = 2L)

        val expense = ExpenseModel.default().copy(
            expensePayer = payer,
            userExpenses = listOf(
                UserExpenseModel(bob, 5.toMoney()),
                UserExpenseModel(bob, 7.toMoney())
            )
        )

        val result = mapper.map(listOf(expense))

        assertEquals(12.toMoney(), result[1L]?.finalBalance)
        assertEquals((-12).toMoney(), result[2L]?.finalBalance)
    }

    @Test
    fun `zero amount entries should not affect balances`() {
        val a = GroupMemberModel.random().copy(id = 1L)
        val b = GroupMemberModel.random().copy(id = 2L)

        val expense = ExpenseModel.default().copy(
            expensePayer = a,
            userExpenses = listOf(
                UserExpenseModel(b, 0.toMoney())
            )
        )

        val result = mapper.map(listOf(expense))

        assertEquals(0.toMoney(), result[1L]?.finalBalance)
        assertEquals(0.toMoney(), result[2L]?.finalBalance)
    }

    @Test
    fun `payer entries inside userExpenses should be ignored`() {
        val payer = GroupMemberModel.random().copy(id = 1L)
        val bob = GroupMemberModel.random().copy(id = 2L)

        val expense = ExpenseModel.default().copy(
            expensePayer = payer,
            userExpenses = listOf(
                UserExpenseModel(payer, 999.toMoney()),
                UserExpenseModel(bob, 10.toMoney())
            )
        )

        val result = mapper.map(listOf(expense))

        assertEquals(10.toMoney(), result[1L]?.finalBalance)
        assertEquals((-10).toMoney(), result[2L]?.finalBalance)
    }

    @Test
    fun `multiple expenses should aggregate consistently`() {
        val a = GroupMemberModel.random().copy(id = 1L)
        val b = GroupMemberModel.random().copy(id = 2L)

        val expenses = (1..100).map {
            ExpenseModel.default().copy(
                expensePayer = a,
                userExpenses = listOf(
                    UserExpenseModel(b, 1.toMoney())
                )
            )
        }

        val result = mapper.map(expenses)

        assertEquals(100.toMoney(), result[1L]?.finalBalance)
        assertEquals((-100).toMoney(), result[2L]?.finalBalance)
    }
}
