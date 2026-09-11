package lol.terabrendon.houseshare2.data.remote.dto

data class AppGroupDto(
    val id: Long,
    val name: String,
    val description: String? = null,
    val userIds: List<Long> = listOf(),
    val users: List<AppUserDto> = listOf(),
    val members: List<GroupMemberDto> = listOf(),
    val imageUrl: String? = null,
)
