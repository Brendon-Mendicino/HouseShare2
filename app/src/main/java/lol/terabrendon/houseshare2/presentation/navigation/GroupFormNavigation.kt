package lol.terabrendon.houseshare2.presentation.navigation

import kotlinx.serialization.Serializable

sealed class GroupFormNavigation : MainNavigation() {

    @Serializable
    data object SelectUsers : GroupFormNavigation()

    @Serializable
    data object GroupInfo : GroupFormNavigation()
}