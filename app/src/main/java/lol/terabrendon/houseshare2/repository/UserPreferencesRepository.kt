package lol.terabrendon.houseshare2.repository

import kotlinx.coroutines.flow.Flow
import lol.terabrendon.houseshare2.UserPreferences
import lol.terabrendon.houseshare2.presentation.navigation.MainDestination

interface UserPreferencesRepository {
    val userPreferencesFlow: Flow<UserPreferences>

    suspend fun updateMainDestination(destination: MainDestination)
}