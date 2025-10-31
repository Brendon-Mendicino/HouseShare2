package lol.terabrendon.houseshare2.domain.usecase

import kotlinx.coroutines.flow.first
import lol.terabrendon.houseshare2.data.repository.AuthRepository
import lol.terabrendon.houseshare2.data.repository.UserPreferencesRepository
import lol.terabrendon.houseshare2.presentation.navigation.MainNavigation
import javax.inject.Inject

class StartingDestinationUseCase @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val userAuthRepository: AuthRepository,
) {
    suspend operator fun invoke(): MainNavigation {
        val destination = userPreferencesRepository.savedDestination.first()

        // If no user is logged in go-back to the login page
        return if (userAuthRepository.loggedUser().isOk) {
            destination
        } else {
            userPreferencesRepository.updateMainDestination(MainNavigation.Login::class)
            MainNavigation.Login
        }
    }
}