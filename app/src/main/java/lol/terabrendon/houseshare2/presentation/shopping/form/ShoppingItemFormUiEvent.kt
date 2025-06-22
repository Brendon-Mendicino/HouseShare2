package lol.terabrendon.houseshare2.presentation.shopping.form

sealed class ShoppingItemFormUiEvent {
    object SubmitSuccess : ShoppingItemFormUiEvent()
    data class SubmitFailure(val error: String) : ShoppingItemFormUiEvent()
}