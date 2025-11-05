package lol.terabrendon.houseshare2.presentation.provider

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf

data class TopBarConfig(
    val navigationIcon: (@Composable () -> Unit)? = null,
)

class TopBarManager {
    private val lock = Any()
    private val _topBarConfig = mutableStateOf<TopBarConfig?>(null)

    val topBarConfig: State<TopBarConfig?> get() = _topBarConfig

    fun setConfig(config: TopBarConfig) {
        synchronized(lock) {
            check(topBarConfig.value == null) { "You are configuring multiple TopBarConfig at the same time! Check that you only have one RegisterTopBarConfig() called!" }
            _topBarConfig.value = config
        }
    }

    fun resetConfig() {
        synchronized(lock) {
            _topBarConfig.value = null
        }
    }
}