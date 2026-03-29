package lol.terabrendon.houseshare2.domain.mapper

import androidx.core.net.toUri
import lol.terabrendon.houseshare2.data.entity.Group
import lol.terabrendon.houseshare2.data.entity.composite.GroupWithUsers
import lol.terabrendon.houseshare2.data.remote.dto.GroupDto
import lol.terabrendon.houseshare2.domain.form.GroupFormState
import lol.terabrendon.houseshare2.domain.model.GroupInfoModel
import lol.terabrendon.houseshare2.domain.model.GroupModel

fun GroupModel.toDto() = GroupDto(
    id = info.groupId,
    name = info.name,
    description = info.description,
    imageUrl = info.imageUrl?.toString(),
    userIds = users.map { it.id },
)

fun GroupModel.toEntity() = Group(
    id = info.groupId,
    name = info.name,
    description = info.description,
    imageUrl = info.imageUrl?.toString(),
)

fun GroupFormState.toModel() = GroupModel(
    info = GroupInfoModel(
        groupId = 0,
        name = name,
        description = description,
        imageUrl = imageUrl?.toUri(),
    ),
    users = users,
)

fun Group.toModel() = GroupInfoModel(
    groupId = id,
    name = name,
    description = description,
    imageUrl = imageUrl?.toUri(),
)

fun GroupWithUsers.toModel() = GroupModel(
    info = GroupInfoModel(
        groupId = group.id,
        name = group.name,
        description = group.description,
        imageUrl = group.imageUrl?.toUri(),
    ),
    users = users.map { it.toModel() },
)

fun GroupDto.toEntity() = Group(
    id = id,
    name = name,
    description = description,
    imageUrl = imageUrl,
)