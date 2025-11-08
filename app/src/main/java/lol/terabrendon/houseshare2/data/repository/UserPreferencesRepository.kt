package lol.terabrendon.houseshare2.data.repository

import kotlinx.coroutines.flow.Flow
import lol.terabrendon.houseshare2.presentation.navigation.MainNavigation

// TODO: rename userData
interface UserPreferencesRepository {
    val savedBackStack: Flow<List<MainNavigation>>

    suspend fun updateBackStack(backStack: List<MainNavigation>)

    val currentLoggedUserId: Flow<Long?>

    val selectedGroupId: Flow<Long?>

    suspend fun updateCurrentLoggedUser(userId: Long?)

    suspend fun updateSelectedGroupId(groupId: Long?)
}