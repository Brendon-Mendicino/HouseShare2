package lol.terabrendon.houseshare2.data.dto

import lol.terabrendon.houseshare2.domain.model.ShoppingItemPriority
import java.time.OffsetDateTime

data class ShoppingItemDto(
    val id: Long,
    val ownerId: Long,
    val groupId: Long,
    val name: String,
    val amount: Int,
    val price: Double?,
    val priority: ShoppingItemPriority,
    val createdAt: OffsetDateTime,
    val check: CheckDto?,
)