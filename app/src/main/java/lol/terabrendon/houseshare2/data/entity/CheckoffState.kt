package lol.terabrendon.houseshare2.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = ShoppingItem::class,
            parentColumns = ["id"],
            childColumns = ["shoppingItemId"],
            onUpdate = ForeignKey.CASCADE,
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["checkingUserId"],
            onUpdate = ForeignKey.CASCADE,
            // TODO: decide if a user is deleted what needs to happen (maybe anonymous or deleted-user?)
            onDelete = ForeignKey.CASCADE,
        ),
    ]
)
// TODO: decide if i want this class to be embedded in `ShoppingItem` or not
// TODO: move this entity into the parent
data class CheckoffState(
    @PrimaryKey(autoGenerate = true)
    val checkoffId: Long = 0,
    @ColumnInfo(index = true)
    val shoppingItemId: Long,
    @ColumnInfo(index = true)
    val checkingUserId: Long,
    @ColumnInfo(defaultValue = "(datetime('now', 'localtime'))")
    val checkoffTimestamp: LocalDateTime = LocalDateTime.now(),
)
