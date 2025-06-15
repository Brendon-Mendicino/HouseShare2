package lol.terabrendon.houseshare2.presentation.navigation

import kotlinx.serialization.Serializable

sealed class GroupFormNavigation : MainNavigation() {

    @Serializable
    object SelectUsers : GroupFormNavigation()

    @Serializable
    object GroupInfo : GroupFormNavigation()

}