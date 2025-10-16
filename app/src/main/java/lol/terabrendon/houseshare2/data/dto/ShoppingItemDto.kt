package lol.terabrendon.houseshare2.data.dto

import lol.terabrendon.houseshare2.domain.model.ShoppingItemPriority

data class ShoppingItemDto(
    val id: Long,
    val ownerId: Long,
    val groupId: Long,
    var name: String,
    var amount: Int,
    var price: Double?,
    var priority: ShoppingItemPriority,
)