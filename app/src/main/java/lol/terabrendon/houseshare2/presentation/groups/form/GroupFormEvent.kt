package lol.terabrendon.houseshare2.presentation.groups.form

import lol.terabrendon.houseshare2.domain.model.UserModel

sealed class GroupFormEvent {
    data class NameChanged(val name: String) : GroupFormEvent()
    data class DescriptionChanged(val description: String) : GroupFormEvent()
    data class UserListClicked(val user: UserModel) : GroupFormEvent()
    data class UserSelectedClicked(val userId: Long) : GroupFormEvent()
    object Submit : GroupFormEvent()
}