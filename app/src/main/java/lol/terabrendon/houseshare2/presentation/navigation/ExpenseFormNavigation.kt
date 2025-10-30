package lol.terabrendon.houseshare2.presentation.navigation

import kotlinx.serialization.Serializable

sealed class ExpenseFormNavigation : MainNavigation() {
    @Serializable
    data object Expense : ExpenseFormNavigation()
}