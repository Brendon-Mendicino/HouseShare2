package lol.terabrendon.houseshare2.domain.mapper

import lol.terabrendon.houseshare2.data.dto.GroupDto
import lol.terabrendon.houseshare2.data.entity.Group
import lol.terabrendon.houseshare2.data.entity.composite.GroupWithUsers
import lol.terabrendon.houseshare2.domain.model.GroupFormState
import lol.terabrendon.houseshare2.domain.model.GroupInfoModel
import lol.terabrendon.houseshare2.domain.model.GroupModel
import lol.terabrendon.houseshare2.domain.model.UserModel
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

    class FormToModel @Inject constructor() : Mapper<GroupFormState, GroupModel> {
        override fun map(it: GroupFormState) = GroupModel(
            info = GroupInfoModel(
                groupId = 0,
                name = it.name,
                description = it.description,
            ),
            users = it.users,
        )
    }

    class EntityToModel @Inject constructor() : Mapper<GroupWithUsers, GroupModel> {
        override fun map(it: GroupWithUsers) = GroupModel(
            info = GroupInfoModel(
                groupId = it.group.id,
                name = it.group.name,
                description = it.group.description,
            ),
            users = it.users.map { UserModel.from(it) },
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