package lol.terabrendon.houseshare2.presentation.screen.groups.form

sealed class GroupMemberFormEvent {
    data class FirstNameChanged(val firstName: String) : GroupMemberFormEvent()
    data class LastNameChanged(val lastName: String) : GroupMemberFormEvent()
    data class PictureChanged(val picture: String) : GroupMemberFormEvent()
    object Submit : GroupMemberFormEvent()
}
