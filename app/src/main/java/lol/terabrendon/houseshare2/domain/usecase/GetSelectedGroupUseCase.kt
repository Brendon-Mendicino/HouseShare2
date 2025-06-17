package lol.terabrendon.houseshare2.domain.usecase

import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import lol.terabrendon.houseshare2.data.repository.GroupRepository
import lol.terabrendon.houseshare2.data.repository.UserPreferencesRepository
import lol.terabrendon.houseshare2.domain.model.GroupModel
import javax.inject.Inject

@ViewModelScoped
class GetSelectedGroupUseCase @Inject constructor(
    private val sharedPreferencesRepository: UserPreferencesRepository,
    private val groupRepository: GroupRepository,
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    fun execute(): Flow<GroupModel?> = sharedPreferencesRepository
        .selectedGroupId
        .flatMapLatest { groupId ->
            groupId?.let {
                groupRepository.findById(it)
            } ?: flowOf(null)
        }
}