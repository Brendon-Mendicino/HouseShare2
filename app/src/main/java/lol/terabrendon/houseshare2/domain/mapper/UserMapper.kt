package lol.terabrendon.houseshare2.domain.mapper

import lol.terabrendon.houseshare2.data.entity.User
import lol.terabrendon.houseshare2.data.remote.dto.UserDto
import lol.terabrendon.houseshare2.domain.model.UserModel

fun UserDto.toEntity() = User(
    id = id,
    username = username,
)

fun User.toModel() = UserModel(
    id = id,
    username = username,
)