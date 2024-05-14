package lol.terabrendon.houseshare2.util

import android.icu.text.NumberFormat
import android.icu.util.Currency
import java.util.Locale

fun Double.currencyFormat(): String {
    val format = NumberFormat.getCurrencyInstance().apply {
        maximumFractionDigits = 2
        currency = Currency.getInstance(Locale.getDefault())
    }

    return format.format(this)
}

fun String.currencyFormat(): Double? {
    val format = NumberFormat.getCurrencyInstance().apply {
        maximumFractionDigits = 2
        currency = Currency.getInstance(Locale.getDefault())
    }

    return format.runCatching { parse(this@currencyFormat).toDouble() }.getOrNull()
}
