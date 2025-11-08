package lol.terabrendon.houseshare2.domain.usecase

import kotlinx.coroutines.flow.first
import lol.terabrendon.houseshare2.data.repository.AuthRepository
import lol.terabrendon.houseshare2.data.repository.UserDataRepository
import lol.terabrendon.houseshare2.presentation.navigation.MainNavigation
import javax.inject.Inject

// TODO: remove this class
class StartingDestinationUseCase @Inject constructor(
    private val userDataRepository: UserDataRepository,
    private val userAuthRepository: AuthRepository,
) {
    suspend operator fun invoke(): List<MainNavigation> {
        val destination = userDataRepository.savedBackStack.first()

        // If no user is logged in go-back to the login page
        return if (userAuthRepository.loggedUser().isOk) {
            destination
        } else {
            listOf(MainNavigation.Login)
        }
    }
}