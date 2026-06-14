package lol.terabrendon.houseshare2.domain.mapper

import lol.terabrendon.houseshare2.data.entity.GroupMember
import lol.terabrendon.houseshare2.data.remote.dto.GroupMemberDto

fun GroupMemberDto.toEntity() = GroupMember(
    id = id,
    username = username,
    groupId = groupId,
    picture = picture,
    userId = userId,
)

fun GroupMember.toDto() = GroupMemberDto(
    id = id,
    username = username,
    picture = picture,
    groupId = groupId,
    userId = userId,
)