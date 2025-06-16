package lol.terabrendon.houseshare2.presentation.util

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf

class FabActionManager {
    private val _fabAction = mutableStateOf<(() -> Unit)?>(null)
    val fabAction: State<(() -> Unit)?> get() = _fabAction

    fun setFabAction(action: (() -> Unit)?) {
        _fabAction.value = action
    }

}