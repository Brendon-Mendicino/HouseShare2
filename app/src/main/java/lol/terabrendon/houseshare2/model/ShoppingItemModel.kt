package lol.terabrendon.houseshare2.model

import lol.terabrendon.houseshare2.entity.ShoppingItem
import java.time.LocalDateTime

data class ShoppingItemModel(
    val id: Int,
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
                name = item.name,
                amount = item.amount,
                price = item.price,
                creationTimestamp = item.creationTimestamp,
                selected = false
            )
        }
    }
}