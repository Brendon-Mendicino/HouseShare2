package lol.terabrendon.houseshare2.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import lol.terabrendon.houseshare2.domain.model.ShoppingItemModel
import java.time.LocalDateTime

@Entity
data class ShoppingItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
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
}

