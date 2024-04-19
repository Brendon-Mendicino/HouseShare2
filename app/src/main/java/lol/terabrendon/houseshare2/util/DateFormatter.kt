package lol.terabrendon.houseshare2.util

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

fun LocalDateTime.inlineFormat(): String {
    val now = LocalDateTime.now()

    // We are on the same day
    if (now.dayOfYear == this.dayOfYear && now.month == this.month && now.year == this.year)
        return this.format(DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault()))

    // The current time is within a year of now
    if (now.minusYears(1) < this)
        return this.format(DateTimeFormatter.ofPattern("dd MMM", Locale.getDefault()))

    return this.format(DateTimeFormatter.ofPattern("dd MMM, yyyy", Locale.getDefault()))
}