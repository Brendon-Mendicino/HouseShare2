package lol.terabrendon.houseshare2.data.dto

import java.time.OffsetDateTime

data class CheckDto(
    val checkingUserId: Long,
    val checkoffTimestamp: OffsetDateTime,
)
