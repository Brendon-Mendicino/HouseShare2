package lol.terabrendon.houseshare2.presentation.groups

sealed class GroupInfoEvent {
    data object ShareGroup : GroupInfoEvent()
}