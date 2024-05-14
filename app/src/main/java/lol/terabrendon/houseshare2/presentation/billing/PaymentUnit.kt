package lol.terabrendon.houseshare2.presentation.billing

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Percent
import androidx.compose.ui.graphics.vector.ImageVector
import lol.terabrendon.houseshare2.R

enum class PaymentUnit {
    Additive,
    Percentage,
    Quota;

    @StringRes
    fun toStringRes(): Int = when (this) {
        Additive -> R.string.additive
        Percentage -> R.string.percentage
        Quota -> R.string.quota
    }

    fun toImageVector(): ImageVector = when (this) {
        Additive -> Icons.Filled.Add
        Percentage -> Icons.Filled.Percent
        Quota -> Icons.Filled.BarChart
    }
}