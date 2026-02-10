package lol.terabrendon.houseshare2.data.local.preferences

import kotlinx.serialization.Serializable
import lol.terabrendon.houseshare2.presentation.navigation.MainNavigation

@Serializable
data class UserData(
    val backStack: List<MainNavigation> = emptyList(),
    val currentLoggedUserId: Long? = null,
    val selectedGroupId: Long? = null,
    val termsAndConditions: Boolean = false,
    val sendAnalytics: Boolean = false,
    val appTheme: Theme = Theme.System,
    val dynamicColors: Boolean = false,
) {
    enum class Theme {
        System,
        Dark,
        Light,
    }
}