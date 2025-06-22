package lol.terabrendon.houseshare2.domain.model

import lol.terabrendon.houseshare2.data.entity.ShoppingItem
import java.time.LocalDateTime

data class ShoppingItemModel(
    val id: Long,
    val ownerId: Long,
    val groupId: Long,
    val name: String,
    val amount: Int,
    val price: Double?,
    val creationTimestamp: LocalDateTime,
    val selected: Boolean,
) {
    companion object {
        @JvmStatic
        fun from(item: ShoppingItem): ShoppingItemModel {
            return ShoppingItemModel(
                id = item.id,
                ownerId = item.ownerId,
                groupId = item.groupId,
                name = item.name,
                amount = item.amount,
                price = item.price,
                creationTimestamp = item.creationTimestamp,
                selected = false
            )
        }

        fun default() = ShoppingItemModel(
            id = 0,
            ownerId = 0,
            groupId = 0,
            name = "",
            amount = 0,
            price = null,
            creationTimestamp = LocalDateTime.now(),
            selected = false,
        )
    }
}