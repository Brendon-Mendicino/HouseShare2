package lol.terabrendon.houseshare2.presentation.navigation

import androidx.annotation.StringRes
import lol.terabrendon.houseshare2.R

enum class MainDestination {
    Cleaning,
    Shopping;

    @StringRes
    fun asResource(): Int {
        return when (this) {
            Cleaning -> R.string.cleaning
            Shopping -> R.string.shopping_list
        }
    }
}