package lol.terabrendon.houseshare2.mapper

import lol.terabrendon.houseshare2.model.BillingBalanceModel
import lol.terabrendon.houseshare2.model.ExpenseModel

class ExpenseBalanceMapper {
    fun map(expenses: List<ExpenseModel>): List<BillingBalanceModel> = expenses
        .asSequence()
        // For each expense we need to computed the debt of the users with respect
        // to the owner, and how much the current owner needs to be paid
        // by all the other users.
        .flatMap { expense ->
            val debts = expense
                .userExpenses
                .filter { userPart -> userPart.user != expense.expenseOwner }
                .map { userPart -> Pair(userPart.user, -userPart.partAmount) }

            // Add the owner of the expense with a positive debt
            val credit = Pair(expense.expenseOwner, debts.sumOf { -it.second })

            debts.plus(credit)
        }
        .groupingBy { it.first }
        .fold({ user, _ -> BillingBalanceModel(user, 0.0) }) { _, balance, (_, expense) ->
            balance.copy(finalBalance = balance.finalBalance + expense)
        }
        .values
        .toList()
}