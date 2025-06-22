package lol.terabrendon.houseshare2.presentation.groups.form

sealed class GroupFormUiEvent {
    object SubmitSuccess : GroupFormUiEvent()
    data class SubmitFailure(val error: String) : GroupFormUiEvent()
}