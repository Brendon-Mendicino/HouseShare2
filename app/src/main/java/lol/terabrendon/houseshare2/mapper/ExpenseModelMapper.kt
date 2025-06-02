package lol.terabrendon.houseshare2.mapper

import lol.terabrendon.houseshare2.model.ExpenseModel
import lol.terabrendon.houseshare2.model.UserExpenseModel
import lol.terabrendon.houseshare2.model.UserModel
import lol.terabrendon.houseshare2.presentation.billing.ExpenseFormState
import lol.terabrendon.houseshare2.presentation.billing.UserPaymentState
import lol.terabrendon.houseshare2.util.Err
import lol.terabrendon.houseshare2.util.Ok
import lol.terabrendon.houseshare2.util.Result
import java.time.LocalDateTime

class ExpenseModelMapper {
    fun map(
        formState: ExpenseFormState,
        expenseOwner: UserModel,
        payments: List<UserPaymentState>,
        creationTimestamp: LocalDateTime = LocalDateTime.now(),
    ): Result<ExpenseModel, String> {
        return Ok(
            ExpenseModel(
                id = 0,
                amount = formState.moneyAmount,
                expenseOwner = expenseOwner,
                expensePayer = formState.payer ?: return Err("Payer from formState was null!"),
                category = formState.category ?: return Err("Category from formState was null!"),
                title = formState.title,
                description = formState.description,
                creationTimestamp = creationTimestamp,
                userExpenses = payments.map {
                    UserExpenseModel(
                        user = it.user,
                        partAmount = it.amountMoney,
                    )
                },
            )
        )
    }
}