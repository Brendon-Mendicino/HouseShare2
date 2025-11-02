package lol.terabrendon.houseshare2.presentation.navigation

import kotlinx.serialization.Serializable

sealed class HomepageNavigation : MainNavigation() {
    @Serializable
    data object Cleaning : HomepageNavigation()

    @Serializable
    data object Shopping : HomepageNavigation()

    @Serializable
    data object ShoppingForm : HomepageNavigation()

    @Serializable
    data class ShoppingItem(val shoppingItemId: Long) : HomepageNavigation()

    @Serializable
    data object Billing : HomepageNavigation()

    @Serializable
    data object Groups : HomepageNavigation()

    @Serializable
    data object GroupForm : HomepageNavigation()

    @Serializable
    data object ExpenseForm : HomepageNavigation()
}