package lol.terabrendon.houseshare2.domain.mapper

import lol.terabrendon.houseshare2.data.dto.GroupDto
import lol.terabrendon.houseshare2.data.entity.Group
import lol.terabrendon.houseshare2.domain.model.GroupModel
import javax.inject.Inject

object GroupMapper {
    class ModelToDto @Inject constructor() : Mapper<GroupModel, GroupDto> {
        override fun map(it: GroupModel) = GroupDto(
            id = it.info.groupId,
            name = it.info.name,
            description = it.info.description,
            userIds = it.users.map { it.id },
        )
    }

    class DtoToEntity @Inject constructor() : Mapper<GroupDto, Group> {
        override fun map(it: GroupDto) = Group(
            id = it.id,
            name = it.name,
            description = it.description,
        )
    }
}