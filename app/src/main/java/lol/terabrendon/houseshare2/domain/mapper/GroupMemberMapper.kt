package lol.terabrendon.houseshare2.domain.mapper

import androidx.core.net.toUri
import lol.terabrendon.houseshare2.data.entity.GroupMember
import lol.terabrendon.houseshare2.data.remote.dto.GroupMemberDto
import lol.terabrendon.houseshare2.domain.model.GroupMemberModel

fun GroupMemberDto.toEntity() = GroupMember(
    id = id,
    firstName = firstName,
    lastName = lastName,
    groupId = groupId,
    picture = picture,
    userId = userId,
)

fun GroupMember.toDto() = GroupMemberDto(
    id = id,
    firstName = firstName,
    lastName = lastName,
    picture = picture,
    groupId = groupId,
    userId = userId,
)

fun GroupMember.toModel() = GroupMemberModel(
    id = id,
    firstName = firstName,
    lastName = lastName,
    groupId = groupId,
    userId = userId,
    picture = picture?.toUri(),
)

fun GroupMemberModel.toEntity() = GroupMember(
    id = id,
    firstName = firstName,
    lastName = lastName,
    groupId = groupId,
    picture = picture?.toString(),
    userId = userId,
)

fun GroupMemberDto.toModel() = GroupMemberModel(
    id = id,
    firstName = firstName,
    lastName = lastName,
    picture = picture?.toUri(),
    groupId = groupId,
    userId = userId,
)