package lol.terabrendon.houseshare2.presentation.navigation

import kotlinx.serialization.Serializable

sealed class ShoppingFormNavigation : MainNavigation() {
    @Serializable
    object ShoppingItem : MainNavigation()
}