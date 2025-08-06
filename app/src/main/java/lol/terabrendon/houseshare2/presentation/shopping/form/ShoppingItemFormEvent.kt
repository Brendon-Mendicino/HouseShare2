package lol.terabrendon.houseshare2.presentation.shopping.form

import lol.terabrendon.houseshare2.domain.model.ShoppingItemPriority

sealed class ShoppingItemFormEvent {
    data class NameChanged(val name: String) : ShoppingItemFormEvent()
    data class AmountChanged(val amount: String) : ShoppingItemFormEvent()
    data class PriceChanged(val price: String) : ShoppingItemFormEvent()
    data class PriorityChanged(val priority: ShoppingItemPriority) : ShoppingItemFormEvent()
    object Submit : ShoppingItemFormEvent()
}
