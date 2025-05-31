package lol.terabrendon.houseshare2.entity.composite

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import lol.terabrendon.houseshare2.entity.Group
import lol.terabrendon.houseshare2.entity.GroupUserCrossRef
import lol.terabrendon.houseshare2.entity.User

data class GroupWithUsers(
    @Embedded
    val group: Group,
    @Relation(
        parentColumn = "groupId",
        entityColumn = "userId",
        associateBy = Junction(GroupUserCrossRef::class)
    )
    val users: List<User>
)
