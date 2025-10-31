package lol.terabrendon.houseshare2.data.repository

import kotlinx.coroutines.flow.Flow
import lol.terabrendon.houseshare2.presentation.navigation.MainNavigation
import kotlin.reflect.KClass

interface UserPreferencesRepository {
    val savedDestination: Flow<MainNavigation>

    val topLevelRoutes: Flow<List<MainNavigation>>

    val currentLoggedUserId: Flow<Long?>

    val selectedGroupId: Flow<Long?>

    suspend fun updateMainDestination(destination: KClass<out MainNavigation>)

    suspend fun updateCurrentLoggedUser(userId: Long?)

    suspend fun updateSelectedGroupId(groupId: Long?)
}