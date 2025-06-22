package lol.terabrendon.houseshare2.presentation.shopping

import lol.terabrendon.houseshare2.domain.model.ShoppingItemModel

sealed class ShoppingScreenEvent {
    data class ItemChecked(val item: ShoppingItemModel) : ShoppingScreenEvent()
    object ItemsDeleted : ShoppingScreenEvent()
}
