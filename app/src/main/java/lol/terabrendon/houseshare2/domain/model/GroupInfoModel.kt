package lol.terabrendon.houseshare2.domain.model

data class GroupInfoModel(
    val groupId: Long,
    val name: String,
    val description: String?,
) {
    companion object {
        fun default() = GroupInfoModel(
            groupId = 0,
            name = "GroupInfoModel",
            description = null,
        )
    }
}