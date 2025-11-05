package lol.terabrendon.houseshare2.presentation.provider

import androidx.compose.runtime.State

class FabActionManager : StateManager<() -> Unit>() {
    val fabAction: State<(() -> Unit)?> get() = _state

    override val lazyMessage: String
        get() = "You are configuring multiple FabActionManager at the same time! Check that you only have one RegisterFabAction() called!"
}