package lol.terabrendon.houseshare2.presentation.shopping

import lol.terabrendon.houseshare2.domain.model.ShoppingItemInfoModel

sealed class ShoppingScreenEvent {
    data class ItemChecked(val item: ShoppingItemInfoModel) : ShoppingScreenEvent()
    object ItemsDeleted : ShoppingScreenEvent()
}
