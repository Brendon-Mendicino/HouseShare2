package lol.terabrendon.houseshare2.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
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
            onUpdate = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = Group::class,
            parentColumns = ["id"],
            childColumns = ["groupId"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE,
        ),
    ],
)
data class Expense(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val amount: Double,
    /**
     * The [User] who created the [Expense]
     */
    @ColumnInfo(index = true)
    val ownerId: Long,
    /**
     * The [User] who payed the [Expense]
     */
    @ColumnInfo(index = true)
    val payerId: Long,
    /**
     * The [Group] where this [Expense] belongs
     */
    @ColumnInfo(index = true)
    val groupId: Long,
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
            groupId = expense.groupId,
            title = expense.title,
            category = expense.category,
            description = expense.description,
            creationTimestamp = expense.creationTimestamp,
        )
    }
}

