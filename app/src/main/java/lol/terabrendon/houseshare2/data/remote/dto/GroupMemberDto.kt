package lol.terabrendon.houseshare2.data.remote.dto

data class GroupMemberDto(
    val id: Long,
    val firstName: String,
    val lastName: String?,
    val picture: String?,
    val groupId: Long,
    val userId: Long?,
)
