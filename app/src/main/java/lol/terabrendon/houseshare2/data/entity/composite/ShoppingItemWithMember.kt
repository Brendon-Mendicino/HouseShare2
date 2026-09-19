package lol.terabrendon.houseshare2.data.entity.composite

import androidx.room.Embedded
import androidx.room.Relation
import lol.terabrendon.houseshare2.data.entity.GroupMember
import lol.terabrendon.houseshare2.data.entity.ShoppingItem

data class ShoppingItemWithMember(
    @Embedded
    val item: ShoppingItem,
    @Relation(
        parentColumn = "ownerId",
        entityColumn = "id",
    )
    val itemOwner: GroupMember,
    @Relation(
        parentColumn = "checkingMemberId",
        entityColumn = "id",
    )
    val checkingMember: GroupMember?,
)