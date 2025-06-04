package lol.terabrendon.houseshare2.domain.mapper

import lol.terabrendon.houseshare2.data.entity.composite.GroupWithUsers
import lol.terabrendon.houseshare2.domain.model.GroupInfoModel
import lol.terabrendon.houseshare2.domain.model.GroupModel
import lol.terabrendon.houseshare2.domain.model.UserModel

class GroupEntityMapper {
    fun map(group: GroupWithUsers): GroupModel = GroupModel(
        info = GroupInfoModel(
            groupId = group.group.id,
            name = group.group.name,
        ),
        users = group.users.map { UserModel.from(it) },
    )
}