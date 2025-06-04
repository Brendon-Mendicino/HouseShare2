package lol.terabrendon.houseshare2.data.entity.composite

import androidx.room.Embedded
import androidx.room.Relation
import lol.terabrendon.houseshare2.data.entity.Expense
import lol.terabrendon.houseshare2.data.entity.Payment
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
        entity = Payment::class,
        parentColumn = "id",
        entityColumn = "expenseId"
    )
    val expensesWithUser: List<PaymentWithUser>,
) {
//    companion object {
//        @JvmStatic
//        fun from(expense: ExpenseModel): ExpenseWithUsers {
//            return ExpenseWithUsers(
//                expense = Expense(
//                    id = expense.id,
//                    amount = expense.amount,
//                    ownerId = expense.expenseOwner.id,
//                    category = expense.category,
//                    title = expense.title,
//                    description = expense.description,
//                ),
//                owner = User.from(expense.expenseOwner),
//                expensesWithUser = expense.userExpenses.map {
//                    PaymentWithUser(
//                        payment = Payment(
//                            expenseId = expense.id,
//                            userId = it.user.id,
//                            amount = it.amount,
//                        ),
//                        user = User.from(it.user)
//                    )
//                }
//            )
//        }
//    }
}