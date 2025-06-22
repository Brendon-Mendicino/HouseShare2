package lol.terabrendon.houseshare2.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import lol.terabrendon.houseshare2.domain.model.ShoppingItemModel
import java.time.LocalDateTime

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["ownerId"],
            onUpdate = ForeignKey.CASCADE,
            // TODO: decide if a user is deleted what needs to happen (maybe anonymous or deleted-user?)
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = Group::class,
            parentColumns = ["id"],
            childColumns = ["groupId"],
            onUpdate = ForeignKey.CASCADE,
            onDelete = ForeignKey.CASCADE,
        )
    ]
)
data class ShoppingItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(index = true)
    val ownerId: Long,
    @ColumnInfo(index = true)
    val groupId: Long,
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
                ownerId = item.ownerId,
                groupId = item.groupId,
                name = item.name,
                amount = item.amount,
                price = item.price,
            )
        }
    }
}

