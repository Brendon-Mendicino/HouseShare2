package lol.terabrendon.houseshare2.domain.mapper

import lol.terabrendon.houseshare2.data.entity.User
import lol.terabrendon.houseshare2.data.remote.dto.UserDto
import lol.terabrendon.houseshare2.domain.model.UserModel
import javax.inject.Inject

object UserMapper {
    class DtoToEntity @Inject constructor() : Mapper<UserDto, User> {
        override fun map(it: UserDto) = User(
            id = it.id,
            username = it.username,
        )
    }

    class EntityToModel @Inject constructor() : Mapper<User, UserModel> {
        override fun map(it: User) = UserModel(id = it.id, username = it.username)
    }
}