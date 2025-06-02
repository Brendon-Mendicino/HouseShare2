package lol.terabrendon.houseshare2.data.entity

import androidx.room.TypeConverter
import java.time.LocalDateTime
import java.time.format.DateTimeParseException

class DateConverter {
    @TypeConverter
    fun fromDb(value: String?): LocalDateTime? = try {
        LocalDateTime.parse(value)
    } catch (_: DateTimeParseException) {
        null
    }

    @TypeConverter
    fun fromLocalDateTime(value: LocalDateTime?): String? = value?.toString()
}