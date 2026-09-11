package lol.terabrendon.houseshare2.domain.model

import java.time.LocalDateTime

data class CheckoffStateModel(
    val checkoffTime: LocalDateTime,
    val checkoffUser: GroupMemberModel,
) {
    companion object {
        @JvmStatic
        fun default() = CheckoffStateModel(
            checkoffTime = LocalDateTime.now(),
            checkoffUser = GroupMemberModel.default(),
        )
    }
}
