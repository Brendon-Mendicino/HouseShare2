package lol.terabrendon.houseshare2.domain.usecase

import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import lol.terabrendon.houseshare2.domain.model.GroupMemberModel

class GetLoggedMemberUseCase @Inject constructor(
    private val getLoggedUserUseCase: GetLoggedUserUseCase,
    private val getSelectedGroupUseCase: GetSelectedGroupUseCase,
) {
    operator fun invoke(): Flow<GroupMemberModel?> {
        return getLoggedUserUseCase()
            .combine(getSelectedGroupUseCase()) { user, group ->
                val member = group?.members?.firstOrNull { it.userId == user?.id }

                member
            }
    }
}