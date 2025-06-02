package lol.terabrendon.houseshare2.data.entity

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import androidx.room.Relation
import androidx.room.TypeConverter
import lol.terabrendon.houseshare2.domain.model.ExpenseCategory
import lol.terabrendon.houseshare2.domain.model.ExpenseModel
import java.time.LocalDateTime

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["ownerId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ]
)
data class Expense(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val amount: Double,
    /**
     * The user who created the expense
     */
    @ColumnInfo(index = true)
    val ownerId: Long,
    /**
     * The user who payed the expense
     */
    @ColumnInfo(index = true)
    val payerId: Long,
    val category: ExpenseCategory,
    val title: String,
    val description: String?,
    // TODO: check if it's possible to use OffsetDateTime
    @ColumnInfo(defaultValue = "(datetime('now', 'localtime'))")
    val creationTimestamp: LocalDateTime = LocalDateTime.now(),
) {
    class Converter {
        @TypeConverter
        fun stringFromExpenseCategory(category: ExpenseCategory?): String? = category?.name

        @TypeConverter
        fun expenseCategoryFromString(category: String?): ExpenseCategory? =
            category?.let { ExpenseCategory.valueOf(category) }
    }

    companion object {
        @JvmStatic
        fun from(expense: ExpenseModel): Expense = Expense(
            id = expense.id,
            amount = expense.amount,
            ownerId = expense.expenseOwner.id,
            payerId = expense.expensePayer.id,
            title = expense.title,
            category = expense.category,
            description = expense.description,
            creationTimestamp = expense.creationTimestamp,
        )
    }
}

/**
 * This class represent if the user has contributed to the payment of the expense
 * and what part he has to take
 */
@Entity(
    primaryKeys = ["expenseId", "userId"],
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = Expense::class,
            parentColumns = ["id"],
            childColumns = ["expenseId"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE,
        ),
    ]
)
data class Payment(
    @ColumnInfo(index = true)
    val expenseId: Long,
    @ColumnInfo(index = true)
    val userId: Long,
    /** What are the total user expenses in the expense */
    val partAmount: Double,
)

data class PaymentWithUser(
    @Embedded
    val payment: Payment,
    @Relation(
        parentColumn = "userId",
        entityColumn = "id",
    )
    val user: User,
)

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