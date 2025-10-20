package lol.terabrendon.houseshare2.data.remote.dto

data class GroupDto(
    val id: Long,
    val name: String,
    val description: String?,
    val userIds: List<Long>,
)
