package lol.terabrendon.houseshare2.domain.mapper

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import lol.terabrendon.houseshare2.domain.form.ExpenseFormState
import lol.terabrendon.houseshare2.domain.model.ExpenseModel
import lol.terabrendon.houseshare2.domain.model.Money
import lol.terabrendon.houseshare2.domain.model.UserExpenseModel
import lol.terabrendon.houseshare2.domain.model.UserModel
import java.time.LocalDateTime

// TODO: remove from here
class ExpenseModelMapper {
    fun map(
        formState: ExpenseFormState,
        expenseOwner: UserModel,
        userParts: List<Pair<Money, UserModel>>,
        creationTimestamp: LocalDateTime = LocalDateTime.now(),
        groupId: Long,
    ): Result<ExpenseModel, String> {
        return Ok(
            ExpenseModel(
                id = 0,
                amount = formState.totalAmountMoney,
                expenseOwner = expenseOwner,
                expensePayer = formState.payer ?: return Err("Payer from formState was null!"),
                groupId = groupId,
                category = formState.category ?: return Err("Category from formState was null!"),
                title = formState.title,
                description = formState.description,
                creationTimestamp = creationTimestamp,
                userExpenses = userParts.map { (amount, user) ->
                    UserExpenseModel(
                        user = user,
                        partAmount = amount,
                    )
                },
            )
        )
    }
}