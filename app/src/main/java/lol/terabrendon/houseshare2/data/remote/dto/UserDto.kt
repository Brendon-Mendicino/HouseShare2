package lol.terabrendon.houseshare2.data.remote.dto

data class UserDto(
    val id: Long,
    val username: String,
    val email: String?,
    val firstName: String?,
    val lastName: String?,
    val picture: String?,
)