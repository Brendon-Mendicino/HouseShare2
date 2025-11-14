package lol.terabrendon.houseshare2.domain.usecase

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import lol.terabrendon.houseshare2.data.repository.GroupRepository
import lol.terabrendon.houseshare2.data.repository.UserDataRepository
import lol.terabrendon.houseshare2.domain.model.GroupModel
import javax.inject.Inject

class GetSelectedGroupUseCase @Inject constructor(
    private val sharedPreferencesRepository: UserDataRepository,
    private val groupRepository: GroupRepository,
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(): Flow<GroupModel?> = sharedPreferencesRepository
        .selectedGroupId
        .flatMapLatest { groupId ->
            if (groupId != null)
                groupRepository.findById(groupId)
            else
                flowOf(null)
        }
}