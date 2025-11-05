package lol.terabrendon.houseshare2.presentation.provider

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State

data class TopBarConfig(
    val navigationIcon: (@Composable () -> Unit)? = null,
)

class TopBarManager : StateManager<TopBarConfig>() {
    val topBarConfig: State<TopBarConfig?> get() = _state

    override val lazyMessage: String
        get() = "You are configuring multiple TopBarConfig at the same time! Check that you only have one RegisterTopBarConfig() called!"
}