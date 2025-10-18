package lol.terabrendon.houseshare2.data.entity.composite

import androidx.room.Embedded
import androidx.room.Relation
import lol.terabrendon.houseshare2.data.entity.ShoppingItem
import lol.terabrendon.houseshare2.data.entity.User

data class ShoppingItemWithUser(
    @Embedded
    val item: ShoppingItem,
    @Relation(
        parentColumn = "ownerId",
        entityColumn = "id",
    )
    val itemOwner: User,
    @Relation(
        parentColumn = "checkingUserId",
        entityColumn = "id",
    )
    val checkingUser: User?,
)