package lol.terabrendon.houseshare2.presentation.groups

import lol.terabrendon.houseshare2.domain.model.GroupInfoModel

sealed class GroupEvent {
    data class GroupSelected(val group: GroupInfoModel) : GroupEvent()
}