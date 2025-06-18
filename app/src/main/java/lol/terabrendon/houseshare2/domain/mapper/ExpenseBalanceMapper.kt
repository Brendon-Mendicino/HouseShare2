package lol.terabrendon.houseshare2.domain.mapper

import lol.terabrendon.houseshare2.domain.model.BillingBalanceModel
import lol.terabrendon.houseshare2.domain.model.ExpenseModel

class ExpenseBalanceMapper {
    fun map(expenses: List<ExpenseModel>): List<BillingBalanceModel> = expenses
        .asSequence()
        // For each expense we need to computed the debt of the users with respect
        // to the payer of the expense, and how much the current payer needs to be paid
        // by all the other users.
        .flatMap { expense ->
            val debts = expense
                .userExpenses
                .filter { userPart -> userPart.user != expense.expensePayer }
                .map { userPart -> Pair(userPart.user, -userPart.partAmount) }

            // Add the payer of the expense with a positive debt
            val credit = Pair(expense.expensePayer, debts.sumOf { -it.second })

            debts.plus(credit)
        }
        .groupingBy { (user, _) -> user }
        // Sum the balances for each user
        .fold({ user, _ -> BillingBalanceModel(user, 0.0) }) { _, balance, (_, expense) ->
            balance.copy(finalBalance = balance.finalBalance + expense)
        }
        .values
        .toList()
}