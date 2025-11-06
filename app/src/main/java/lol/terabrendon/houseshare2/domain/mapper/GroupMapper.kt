package lol.terabrendon.houseshare2.domain.mapper

import lol.terabrendon.houseshare2.data.entity.Group
import lol.terabrendon.houseshare2.data.entity.composite.GroupWithUsers
import lol.terabrendon.houseshare2.data.remote.dto.GroupDto
import lol.terabrendon.houseshare2.domain.model.GroupFormState
import lol.terabrendon.houseshare2.domain.model.GroupInfoModel
import lol.terabrendon.houseshare2.domain.model.GroupModel

fun GroupModel.toDto() = GroupDto(
    id = info.groupId,
    name = info.name,
    description = info.description,
    userIds = users.map { it.id },
)

fun GroupFormState.toModel() = GroupModel(
    info = GroupInfoModel(
        groupId = 0,
        name = name,
        description = description,
    ),
    users = users,
)

fun GroupWithUsers.toModel() = GroupModel(
    info = GroupInfoModel(
        groupId = group.id,
        name = group.name,
        description = group.description,
    ),
    users = users.map { it.toModel() },
)

fun GroupDto.toEntity() = Group(
    id = id,
    name = name,
    description = description,
)