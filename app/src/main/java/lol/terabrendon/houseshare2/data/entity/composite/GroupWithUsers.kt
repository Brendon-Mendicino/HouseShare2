package lol.terabrendon.houseshare2.data.entity.composite

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import lol.terabrendon.houseshare2.data.entity.Group
import lol.terabrendon.houseshare2.data.entity.GroupMember
import lol.terabrendon.houseshare2.data.entity.GroupUserCrossRef
import lol.terabrendon.houseshare2.data.entity.User

data class GroupWithUsers(
    @Embedded
    val group: Group,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        entity = User::class,
        associateBy = Junction(
            value = GroupUserCrossRef::class,
            parentColumn = "groupId",
            entityColumn = "userId",
        )
    )
    val users: List<User>,
    @Relation(
        parentColumn = "id",
        entityColumn = "groupId"
    )
    val members: List<GroupMember>,
)
