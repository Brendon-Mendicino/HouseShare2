package lol.terabrendon.houseshare2.domain.mapper

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import lol.terabrendon.houseshare2.domain.model.ExpenseModel
import lol.terabrendon.houseshare2.domain.model.UserExpenseModel
import lol.terabrendon.houseshare2.domain.model.UserModel
import lol.terabrendon.houseshare2.presentation.billing.ExpenseFormState
import lol.terabrendon.houseshare2.presentation.billing.UserPaymentState
import java.time.LocalDateTime

class ExpenseModelMapper {
    fun map(
        formState: ExpenseFormState,
        expenseOwner: UserModel,
        payments: List<UserPaymentState>,
        creationTimestamp: LocalDateTime = LocalDateTime.now(),
        groupId: Long,
    ): Result<ExpenseModel, String> {
        return Ok(
            ExpenseModel(
                id = 0,
                amount = formState.moneyAmount,
                expenseOwner = expenseOwner,
                expensePayer = formState.payer ?: return Err("Payer from formState was null!"),
                groupId = groupId,
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