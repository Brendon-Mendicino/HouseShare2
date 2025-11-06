package lol.terabrendon.houseshare2.domain.mapper

import androidx.core.net.toUri
import lol.terabrendon.houseshare2.data.entity.User
import lol.terabrendon.houseshare2.data.remote.dto.UserDto
import lol.terabrendon.houseshare2.domain.model.UserModel

fun UserDto.toEntity() = User(
    id = id,
    username = username,
    email = email,
    firstName = firstName,
    lastName = lastName,
    picture = picture,
)

fun UserDto.toModel() = UserModel(
    id = id,
    username = username,
    email = email,
    firstName = firstName,
    lastName = lastName,
    picture = picture?.toUri(),
)

/**
 * The [User.id] is left uninitialized, this is done on purpose for entities
 */
fun UserModel.toEntity() = User(
    id = 0,
    username = username,
    email = email,
    firstName = firstName,
    lastName = lastName,
    picture = picture?.toString(),
)

fun User.toModel() = UserModel(
    id = id,
    username = username,
    email = email,
    firstName = firstName,
    lastName = lastName,
    picture = picture?.toUri(),
)