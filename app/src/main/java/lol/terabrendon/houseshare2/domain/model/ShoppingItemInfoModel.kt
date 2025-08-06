package lol.terabrendon.houseshare2.domain.model

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material.icons.filled.WatchLater
import androidx.compose.ui.graphics.vector.ImageVector
import lol.terabrendon.houseshare2.R
import lol.terabrendon.houseshare2.data.entity.ShoppingItem
import java.time.LocalDateTime

data class ShoppingItemInfoModel(
    val id: Long,
    val ownerId: Long,
    val groupId: Long,
    val name: String,
    val amount: Int,
    val price: Double?,
    val creationTimestamp: LocalDateTime,
    val priority: ShoppingItemPriority,
) {
    companion object {
        @JvmStatic
        fun from(item: ShoppingItem): ShoppingItemInfoModel {
            return ShoppingItemInfoModel(
                id = item.id,
                ownerId = item.ownerId,
                groupId = item.groupId,
                name = item.name,
                amount = item.amount,
                price = item.price,
                creationTimestamp = item.creationTimestamp,
                priority = item.priority,
            )
        }

        @JvmStatic
        fun default() = ShoppingItemInfoModel(
            id = 0,
            ownerId = 0,
            groupId = 0,
            name = "",
            amount = 0,
            price = null,
            creationTimestamp = LocalDateTime.now(),
            priority = ShoppingItemPriority.Later,
        )
    }
}

enum class ShoppingItemPriority {
    Now,
    Soon,
    Later;

    @StringRes
    fun toStringRes(): Int = when (this) {
        Now -> R.string.now
        Soon -> R.string.soon
        Later -> R.string.later
    }

    fun toImageVector(): ImageVector = when (this) {
        Now -> Icons.AutoMirrored.Filled.DirectionsRun
        Soon -> Icons.Filled.WarningAmber
        Later -> Icons.Filled.WatchLater
    }
}

