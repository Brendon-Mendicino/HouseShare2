package lol.terabrendon.houseshare2.domain.usecase

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import lol.terabrendon.houseshare2.data.repository.UserDataRepository
import lol.terabrendon.houseshare2.data.repository.UserRepository
import lol.terabrendon.houseshare2.domain.model.UserModel
import javax.inject.Inject

class GetLoggedUserUseCase @Inject constructor(
    private val sharedPreferencesRepository: UserDataRepository,
    private val userRepository: UserRepository,
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(): Flow<UserModel?> = sharedPreferencesRepository
        .currentLoggedUserId
        // TODO: should i get the user from remote?
        .flatMapLatest { userId ->
            if (userId != null)
                userRepository.findById(userId)
            else
                flowOf(null)
        }
}