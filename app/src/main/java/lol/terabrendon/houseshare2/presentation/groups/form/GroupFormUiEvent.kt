package lol.terabrendon.houseshare2.presentation.groups.form

import io.github.brendonmendicino.aformvalidator.annotation.ValidationError

sealed class GroupFormUiEvent {
    object SubmitSuccess : GroupFormUiEvent()
    data class SubmitFailure(val error: ValidationError) : GroupFormUiEvent()
}