package lol.terabrendon.houseshare2.presentation.navigation

import androidx.annotation.StringRes
import lol.terabrendon.houseshare2.R
import lol.terabrendon.houseshare2.UserPreferences

enum class MainDestination {
    Loading,
    Cleaning,
    Shopping,
    Billing;

    companion object {
        @JvmStatic
        fun from(dest: UserPreferences.MainDestination): MainDestination = when (dest) {
            UserPreferences.MainDestination.CLEANING -> Cleaning
            UserPreferences.MainDestination.SHOPPING -> Shopping
            UserPreferences.MainDestination.BILLING -> Billing
            UserPreferences.MainDestination.UNSPECIFIED -> Cleaning
            UserPreferences.MainDestination.UNRECOGNIZED -> Cleaning
        }
    }

    fun toPreferences(): UserPreferences.MainDestination = when (this) {
        Cleaning -> UserPreferences.MainDestination.CLEANING
        Shopping -> UserPreferences.MainDestination.SHOPPING
        Billing -> UserPreferences.MainDestination.BILLING
        Loading -> throw IllegalArgumentException("Cannot call toPreferences of $Loading.")
    }

    @StringRes
    fun asResource(): Int {
        return when (this) {
            Cleaning -> R.string.cleaning
            Shopping -> R.string.shopping_list
            Billing -> R.string.billing
            Loading -> R.string.loading
        }
    }
}