package lol.terabrendon.houseshare2.presentation.navigation

import androidx.annotation.StringRes
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import kotlinx.serialization.Serializable
import lol.terabrendon.houseshare2.R
import lol.terabrendon.houseshare2.UserPreferences
import kotlin.reflect.KClass

sealed class MainNavigation {
    @Serializable
    object Loading : MainNavigation()

    @Serializable
    object Cleaning : MainNavigation()

    @Serializable
    object Shopping : MainNavigation()

    @Serializable
    object Billing : MainNavigation()

    @Serializable
    data class Groups(val currentUserId: Long) : MainNavigation()

    @Serializable
    object GroupForm : MainNavigation()

    companion object {
        fun KClass<out MainNavigation>.toPreferences(): Result<UserPreferences.MainDestination, Throwable> {
            return Ok(
                when (this) {
                    Cleaning::class -> UserPreferences.MainDestination.CLEANING
                    Shopping::class -> UserPreferences.MainDestination.SHOPPING
                    Billing::class -> UserPreferences.MainDestination.BILLING
                    Groups::class -> UserPreferences.MainDestination.GROUPS
                    else -> return Err(Throwable("Cannot call toPreferences of ${this.qualifiedName}."))
                }
            )
        }
    }

    @StringRes
    fun asResource() = when (this) {
        is Cleaning -> R.string.cleaning
        is Shopping -> R.string.shopping_list
        is Billing -> R.string.billing
        is Groups -> R.string.groups
        is Loading -> R.string.loading
        is GroupForm -> TODO()
        is GroupFormNavigation.GroupInfo -> TODO()
        is GroupFormNavigation.SelectUsers -> TODO()
    }
}

