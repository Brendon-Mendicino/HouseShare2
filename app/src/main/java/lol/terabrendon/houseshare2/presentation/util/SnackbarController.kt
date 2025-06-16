package lol.terabrendon.houseshare2.presentation.util

import androidx.compose.material3.SnackbarDuration
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow

data class SnackbarEvent(
    val message: String,
    val action: SnackbarAction? = null,
    val duration: SnackbarDuration = if (action == null) SnackbarDuration.Short else SnackbarDuration.Indefinite,
)

data class SnackbarAction(
    val name: String,
    val action: () -> Unit,
)

object SnackbarController {
    private val _events = Channel<SnackbarEvent>()
    val events = _events.receiveAsFlow()

    suspend fun sendEvent(event: SnackbarEvent) {
        _events.send(event)
    }
}