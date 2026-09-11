package lol.terabrendon.houseshare2.data.remote.dto

import java.time.OffsetDateTime

data class CheckDto(
    val checkingMemberId: Long,
    val checkoffTimestamp: OffsetDateTime,
)
