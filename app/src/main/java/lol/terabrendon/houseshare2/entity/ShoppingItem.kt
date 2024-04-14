package lol.terabrendon.houseshare2.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import lol.terabrendon.houseshare2.model.ShoppingItemModel
import java.time.LocalDateTime
import java.time.format.DateTimeParseException

@Entity
data class ShoppingItem(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val amount: Int,
    val price: Double?,
    @ColumnInfo(defaultValue = "(datetime('now', 'localtime'))")
    val creationTimestamp: LocalDateTime = LocalDateTime.now(),
) {
    companion object {
        @JvmStatic
        fun from(item: ShoppingItemModel): ShoppingItem {
            return ShoppingItem(
                name = item.name,
                amount = item.amount,
                price = item.price,
            )
        }
    }

    class Converter {
        @TypeConverter
        fun fromDb(value: String?): LocalDateTime? = try {
            LocalDateTime.parse(value)
        } catch (e: DateTimeParseException) {
            null
        }

        @TypeConverter
        fun fromLocalDateTime(value: LocalDateTime?): String? = value?.toString()
    }
}

