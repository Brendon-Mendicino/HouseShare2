package lol.terabrendon.houseshare2.presentation.navigation

import androidx.annotation.StringRes
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import kotlinx.serialization.Serializable
import lol.terabrendon.houseshare2.R
import lol.terabrendon.houseshare2.UserPreferences
import lol.terabrendon.houseshare2.presentation.navigation.ExpenseFormNavigation.Expense
import lol.terabrendon.houseshare2.presentation.navigation.GroupFormNavigation.GroupInfo
import lol.terabrendon.houseshare2.presentation.navigation.GroupFormNavigation.SelectUsers
import lol.terabrendon.houseshare2.presentation.navigation.HomepageNavigation.Billing
import lol.terabrendon.houseshare2.presentation.navigation.HomepageNavigation.Cleaning
import lol.terabrendon.houseshare2.presentation.navigation.HomepageNavigation.ExpenseForm
import lol.terabrendon.houseshare2.presentation.navigation.HomepageNavigation.GroupForm
import lol.terabrendon.houseshare2.presentation.navigation.HomepageNavigation.Groups
import lol.terabrendon.houseshare2.presentation.navigation.HomepageNavigation.Shopping
import lol.terabrendon.houseshare2.presentation.navigation.HomepageNavigation.ShoppingForm
import lol.terabrendon.houseshare2.presentation.navigation.LoginNavigation.UserLogin
import lol.terabrendon.houseshare2.presentation.navigation.ShoppingFormNavigation.ShoppingItem
import kotlin.reflect.KClass

sealed class MainNavigation {
    @Serializable
    data object Loading : MainNavigation()

    @Serializable
    data object Homepage : MainNavigation()

    @Serializable
    data object Login : MainNavigation()

    companion object {
        fun KClass<out MainNavigation>.toPreferences(): Result<UserPreferences.MainDestination, Throwable> {
            return Ok(
                when (this) {
                    Cleaning::class -> UserPreferences.MainDestination.CLEANING
                    Shopping::class -> UserPreferences.MainDestination.SHOPPING
                    Billing::class -> UserPreferences.MainDestination.BILLING
                    Groups::class -> UserPreferences.MainDestination.GROUPS
                    Login::class -> UserPreferences.MainDestination.LOGIN
                    else -> return Err(Throwable("Cannot call toPreferences of ${this.qualifiedName}."))
                }
            )
        }

        fun subclasses(): List<KClass<out MainNavigation>> = listOf(
            Cleaning::class,
            Shopping::class,
            ShoppingForm::class,
            ShoppingItem::class,
            Billing::class,
            Groups::class,
            Loading::class,
            GroupForm::class,
            ExpenseForm::class,
            GroupInfo::class,
            SelectUsers::class,
            Expense::class,
            UserLogin::class,
            Login::class,
        )
    }

    @StringRes
    fun asResource() = when (this) {
        is Cleaning -> R.string.cleaning
        is Shopping -> R.string.shopping_list
        is Billing -> R.string.billing
        is Groups -> R.string.groups
        is Loading -> R.string.loading
        UserLogin,
        Login -> R.string.login
        is GroupForm -> TODO()
        is ExpenseForm -> TODO()
        is GroupInfo -> TODO()
        is SelectUsers -> TODO()
        is Expense -> TODO()
        is ShoppingForm -> TODO()
        is ShoppingItem -> TODO()
        is Homepage -> TODO()
    }
}

