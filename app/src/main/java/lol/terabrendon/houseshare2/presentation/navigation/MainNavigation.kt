package lol.terabrendon.houseshare2.presentation.navigation

import androidx.annotation.StringRes
import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable
import lol.terabrendon.houseshare2.R
import lol.terabrendon.houseshare2.presentation.navigation.HomepageNavigation.Billing
import lol.terabrendon.houseshare2.presentation.navigation.HomepageNavigation.Cleaning
import lol.terabrendon.houseshare2.presentation.navigation.HomepageNavigation.ExpenseForm
import lol.terabrendon.houseshare2.presentation.navigation.HomepageNavigation.GroupInfoForm
import lol.terabrendon.houseshare2.presentation.navigation.HomepageNavigation.GroupUsersForm
import lol.terabrendon.houseshare2.presentation.navigation.HomepageNavigation.Groups
import lol.terabrendon.houseshare2.presentation.navigation.HomepageNavigation.Shopping
import lol.terabrendon.houseshare2.presentation.navigation.HomepageNavigation.ShoppingForm
import lol.terabrendon.houseshare2.presentation.navigation.HomepageNavigation.ShoppingItem
import lol.terabrendon.houseshare2.presentation.navigation.HomepageNavigation.UserProfile

@Serializable
sealed class MainNavigation : NavKey {
    @Serializable
    data object Loading : MainNavigation()

    @Serializable
    data object Login : MainNavigation()

    companion object {

        @JvmStatic
        val homepageRoutes = listOf<MainNavigation>(
            Groups, Shopping, Billing, Cleaning,
            UserProfile
        )

        @JvmStatic
        val topLevelRoutes = listOf(Login) + homepageRoutes
    }

    @StringRes
    fun asResource() = when (this) {
        is Loading -> R.string.loading
        is Login -> R.string.login
        is Cleaning -> R.string.cleaning
        is Shopping -> R.string.shopping_list
        is ShoppingItem -> R.string.description
        is ShoppingForm -> R.string.new_item
        is Billing -> R.string.billing
        is ExpenseForm -> R.string.new_expense
        is Groups -> R.string.groups
        is GroupUsersForm,
        is GroupInfoForm -> R.string.new_group

        is UserProfile -> R.string.profile
    }
}

