package lol.terabrendon.houseshare2.presentation.screen.groups

sealed class GroupInfoEvent {
    data object ShareGroup : GroupInfoEvent()
}