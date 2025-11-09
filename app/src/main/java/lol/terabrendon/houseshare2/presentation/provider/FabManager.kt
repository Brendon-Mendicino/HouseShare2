package lol.terabrendon.houseshare2.presentation.provider

import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State

sealed class FabConfig(
    open val visible: Boolean?,
    open val expanded: Boolean?,
) {
    data class Fab(
        override val visible: Boolean? = null,
        override val expanded: Boolean? = null,
        val text: String? = null,
        val icon: (@Composable () -> Unit)? = null,
        val onClick: (() -> Unit)? = null,
    ) : FabConfig(visible, expanded)

    data class Toolbar(
        override val visible: Boolean? = null,
        override val expanded: Boolean? = null,
        // TODO: decide if `expanded` needs to be kept
        val content: (@Composable RowScope.(expanded: Boolean) -> Unit)? = null,
        val fab: Fab,
    ) : FabConfig(visible, expanded)
}

class FabManager : StateManager<FabConfig>() {
    val fabConfig: State<FabConfig?> get() = _state

//    override val lazyMessage: String
//        get() = "You are configuring multiple FabConfig at the same time! Check that you only have one RegisterFabConfig() called!"
}