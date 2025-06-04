package lol.terabrendon.houseshare2.data.repository

import kotlinx.coroutines.flow.Flow
import lol.terabrendon.houseshare2.presentation.navigation.MainNavigation
import kotlin.reflect.KClass

interface UserPreferencesRepository {
    val savedDestination: Flow<MainNavigation>

    suspend fun updateMainDestination(destination: KClass<out MainNavigation>)
}