package lol.terabrendon.houseshare2.presentation.shopping.form

sealed class ShoppingItemFormEvent {
    data class NameChanged(val name: String) : ShoppingItemFormEvent()
    data class AmountChanged(val amount: String) : ShoppingItemFormEvent()
    data class PriceChanged(val price: String) : ShoppingItemFormEvent()
    object Submit : ShoppingItemFormEvent()
}
