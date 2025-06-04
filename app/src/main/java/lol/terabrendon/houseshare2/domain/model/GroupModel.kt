package lol.terabrendon.houseshare2.domain.model

data class GroupModel(
    val info: GroupInfoModel,
    val users: List<UserModel>,
)
