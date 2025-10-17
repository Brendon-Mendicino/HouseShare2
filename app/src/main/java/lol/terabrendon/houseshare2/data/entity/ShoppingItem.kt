package lol.terabrendon.houseshare2.data.entity

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import lol.terabrendon.houseshare2.domain.model.ShoppingItemPriority
import java.time.LocalDateTime

@Entity(
    indices = [
        Index("checkingUserId")
    ],
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
    val priority: ShoppingItemPriority,
    @Embedded
    val checkoff: CheckoffState?,
) {
    data class CheckoffState(
        val checkingUserId: Long,
        val checkoffTimestamp: LocalDateTime = LocalDateTime.now(),
    )
}

