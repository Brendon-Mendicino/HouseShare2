package lol.terabrendon.houseshare2.data.repository

import kotlinx.coroutines.flow.Flow
import lol.terabrendon.houseshare2.data.local.preferences.UserData
import lol.terabrendon.houseshare2.presentation.navigation.MainNavigation

interface UserDataRepository {
    sealed interface Update {
        data class BackStack(val backStack: List<MainNavigation>) : Update
        data class LoggedUserId(val userId: Long?) : Update
        data class SelectedGroupId(val groupId: Long?) : Update
        data class TermsConditions(val accept: Boolean) : Update
        data class SendAnalytics(val accept: Boolean) : Update
        data class AppTheme(val theme: UserData.Theme) : Update
        data class DynamicColors(val dynamicColors: Boolean) : Update
    }

    val savedBackStack: Flow<List<MainNavigation>>
    val currentLoggedUserId: Flow<Long?>
    val selectedGroupId: Flow<Long?>
    val termsAndConditions: Flow<Boolean>
    val sendAnalytics: Flow<Boolean>
    val data: Flow<UserData>

    suspend fun update(update: Update)
}