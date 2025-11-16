package lol.terabrendon.houseshare2.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * This class represent if the user is in debt for the payment contribution
 * of the expense.
 */
@Entity(
    indices = [
        Index("userId"),
        Index("expenseId"),
        Index(value = ["expenseId", "userId"], unique = true),
    ],
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.Companion.CASCADE,
        ),
        ForeignKey(
            entity = Expense::class,
            parentColumns = ["id"],
            childColumns = ["expenseId"],
            onDelete = ForeignKey.Companion.CASCADE,
            onUpdate = ForeignKey.Companion.CASCADE,
        ),
    ]
)
data class ExpensePart(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val expenseId: Long,
    val userId: Long,
    /**
     * What are the total user debts in the expense. This value represents cents, this means
     * that 1 euro is equal to 100 partAmount.
     */
    val partAmount: Long,
)