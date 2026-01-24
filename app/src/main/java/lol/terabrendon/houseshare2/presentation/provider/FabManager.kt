package lol.terabrendon.houseshare2.presentation.provider

import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.Composable
import kotlinx.coroutines.flow.StateFlow
import lol.terabrendon.houseshare2.presentation.navigation.MainNavigation
import lol.terabrendon.houseshare2.presentation.util.UiText

sealed class FabConfig(
    open val visible: Boolean?,
    open val expanded: Boolean?,
    open val route: MainNavigation?,
) {
    fun withRoute(route: MainNavigation) = when (this) {
        is Fab -> copy(route = route)
        is Toolbar -> copy(route = route)
    }

    data class Fab(
        override val visible: Boolean? = null,
        override val expanded: Boolean? = null,
        override val route: MainNavigation? = null,
        val text: UiText? = null,
        val icon: (@Composable () -> Unit)? = null,
        val onClick: (() -> Unit)? = null,
    ) : FabConfig(visible, expanded, route)

    data class Toolbar(
        override val visible: Boolean? = null,
        override val expanded: Boolean? = null,
        override val route: MainNavigation? = null,
        // TODO: decide if `expanded` needs to be kept
        val content: (@Composable RowScope.(expanded: Boolean) -> Unit)? = null,
        val fab: Fab,
    ) : FabConfig(visible, expanded, route)
}

class FabManager : StateManager<FabConfig>() {
    val fabConfig: StateFlow<FabConfig?> get() = _state

//    override val lazyMessage: String
//        get() = "You are configuring multiple FabConfig at the same time! Check that you only have one RegisterFabConfig() called!"
}