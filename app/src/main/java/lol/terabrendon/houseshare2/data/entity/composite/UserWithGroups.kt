package lol.terabrendon.houseshare2.data.entity.composite

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import lol.terabrendon.houseshare2.data.entity.Group
import lol.terabrendon.houseshare2.data.entity.GroupUserCrossRef
import lol.terabrendon.houseshare2.data.entity.User

data class UserWithGroups(
    @Embedded
    val user: User,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        entity = Group::class,
        associateBy = Junction(
            value = GroupUserCrossRef::class,
            parentColumn = "userId",
            entityColumn = "groupId",
        )
    )
    val groups: List<Group>,
)
