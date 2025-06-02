package lol.terabrendon.houseshare2.domain.model

import lol.terabrendon.houseshare2.data.entity.User

data class UserModel(
    val id: Long,
    val username: String,
) {
    companion object {
        @JvmStatic
        fun from(user: User): UserModel = UserModel(
            id = user.id,
            username = user.username,
        )

        @JvmStatic
        fun default(): UserModel = UserModel(
            id = 0,
            username = "Username",
        )
    }
}
