package lol.terabrendon.houseshare2.domain.mapper

import lol.terabrendon.houseshare2.domain.model.BillingBalanceModel
import lol.terabrendon.houseshare2.domain.model.ExpenseModel
import lol.terabrendon.houseshare2.domain.model.UserModel
import lol.terabrendon.houseshare2.domain.model.sum
import lol.terabrendon.houseshare2.domain.model.toMoney
import javax.inject.Inject

// TODO: remove from here
class ExpenseBalanceMapper @Inject constructor() {
    /**
     * Map a list of [ExpenseModel] to a map of [UserModel.id] to [BillingBalanceModel].
     */
    fun map(expenses: List<ExpenseModel>): Map<Long, BillingBalanceModel> = expenses
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
            val credit = Pair(expense.expensePayer, debts.map { -it.second }.sum())

            debts.plus(credit)
        }
        .groupingBy { (user, _) -> user.id }
        // Sum the balances for each user
        .fold({ userId, (user, _) ->
            BillingBalanceModel(
                user,
                0.toMoney()
            )
        }) { _, balance, (_, expense) ->
            balance.copy(finalBalance = balance.finalBalance + expense)
        }
}