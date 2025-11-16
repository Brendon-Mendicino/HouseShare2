package lol.terabrendon.houseshare2.presentation.shopping

sealed class ShoppingItemEvent {
    data class NameChanged(val name: String) : ShoppingItemEvent()
    data class PriceChanged(val price: String) : ShoppingItemEvent()
    data class QuantityChanged(val quantity: String) : ShoppingItemEvent()
    data object Toggled : ShoppingItemEvent()
}