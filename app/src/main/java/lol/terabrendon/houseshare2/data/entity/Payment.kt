package lol.terabrendon.houseshare2.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey

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
data class Payment(
    @ColumnInfo(index = true)
    val expenseId: Long,
    @ColumnInfo(index = true)
    val userId: Long,
    /** What are the total user expenses in the expense */
    val partAmount: Double,
)