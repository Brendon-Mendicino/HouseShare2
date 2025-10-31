package lol.terabrendon.houseshare2.domain.mapper

import lol.terabrendon.houseshare2.data.entity.User
import lol.terabrendon.houseshare2.data.remote.dto.UserDto
import lol.terabrendon.houseshare2.domain.model.UserModel

fun UserDto.toEntity() = User(
    id = id,
    username = username,
)

fun UserDto.toModel() = UserModel(
    id = id,
    username = username,
)

/**
 * The [User.id] is left uninitialized, this is done on purpose for entities
 */
fun UserModel.toEntity() = User(
    id = 0,
    username = username,
)

fun User.toModel() = UserModel(
    id = id,
    username = username,
)