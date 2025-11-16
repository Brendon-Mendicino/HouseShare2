package lol.terabrendon.houseshare2.data.entity.composite

import androidx.room.Embedded
import androidx.room.Relation
import lol.terabrendon.houseshare2.data.entity.Expense
import lol.terabrendon.houseshare2.data.entity.ExpensePart
import lol.terabrendon.houseshare2.data.entity.User

data class ExpenseWithUsers(
    @Embedded
    val expense: Expense,
    @Relation(
        parentColumn = "ownerId",
        entityColumn = "id",
    )
    val owner: User,
    @Relation(
        parentColumn = "payerId",
        entityColumn = "id",
    )
    val payer: User,
    @Relation(
        entity = ExpensePart::class,
        parentColumn = "id",
        entityColumn = "expenseId"
    )
    val expensesWithUser: List<PaymentWithUser>,
)