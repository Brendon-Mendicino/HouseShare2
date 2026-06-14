package lol.terabrendon.houseshare2.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    indices = [
        Index("userId"),
        Index("groupId"),
    ],
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.Companion.SET_NULL,
            onUpdate = ForeignKey.Companion.CASCADE,
        ),
        ForeignKey(
            entity = Group::class,
            parentColumns = ["id"],
            childColumns = ["groupId"],
            onDelete = ForeignKey.Companion.CASCADE,
            onUpdate = ForeignKey.Companion.CASCADE,
        ),
    ]
)
data class GroupMember(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val username: String,
    val groupId: Long,
    val picture: String?,
    val userId: Long?,
)
