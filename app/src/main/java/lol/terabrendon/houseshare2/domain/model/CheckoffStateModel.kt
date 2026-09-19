package lol.terabrendon.houseshare2.domain.model

import java.time.LocalDateTime

data class CheckoffStateModel(
    val checkoffTime: LocalDateTime,
    val checkoffMember: GroupMemberModel,
) {
    companion object {
        @JvmStatic
        fun default() = CheckoffStateModel(
            checkoffTime = LocalDateTime.now(),
            checkoffMember = GroupMemberModel.default(),
        )
    }
}
