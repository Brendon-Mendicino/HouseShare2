package lol.terabrendon.houseshare2.domain.mapper

import lol.terabrendon.houseshare2.data.dto.UserDto
import lol.terabrendon.houseshare2.data.entity.User
import javax.inject.Inject

object UserMapper {
    class DtoToEntity @Inject constructor() : Mapper<UserDto, User> {
        override fun map(it: UserDto) = User(
            id = it.id,
            username = it.username,
        )
    }
}