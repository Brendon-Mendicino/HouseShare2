package lol.terabrendon.houseshare2.domain.mapper

import lol.terabrendon.houseshare2.domain.model.GroupFormState
import lol.terabrendon.houseshare2.domain.model.GroupInfoModel
import lol.terabrendon.houseshare2.domain.model.GroupModel

class GroupFormStateMapper {
    fun map(group: GroupFormState) = GroupModel(
        info = GroupInfoModel(
            groupId = 0,
            name = group.name,
        ),
        users = group.users,
    )
}