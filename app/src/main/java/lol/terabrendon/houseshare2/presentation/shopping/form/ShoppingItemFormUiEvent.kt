package lol.terabrendon.houseshare2.presentation.shopping.form

import io.github.brendonmendicino.aformvalidator.annotation.ValidationError

sealed class ShoppingItemFormUiEvent {
    object SubmitSuccess : ShoppingItemFormUiEvent()
    data class SubmitFailure(val error: ValidationError) : ShoppingItemFormUiEvent()
}