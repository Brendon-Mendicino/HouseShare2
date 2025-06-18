package lol.terabrendon.houseshare2.domain.usecase

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import lol.terabrendon.houseshare2.data.repository.UserPreferencesRepository
import lol.terabrendon.houseshare2.data.repository.UserRepository
import lol.terabrendon.houseshare2.domain.model.UserModel
import javax.inject.Inject

class GetLoggedUserUseCase @Inject constructor(
    private val sharedPreferencesRepository: UserPreferencesRepository,
    private val userRepository: UserRepository,
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    fun execute(): Flow<UserModel?> = sharedPreferencesRepository
        .currentLoggedUserId
        .flatMapLatest { userId ->
            userId
                ?.let { userRepository.findById(it) }
                ?: flowOf(null)
        }
}