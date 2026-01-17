package lol.terabrendon.houseshare2.presentation.util

import androidx.compose.material3.SnackbarDuration
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow

data class SnackbarEvent(
    val message: UiText,
    val withDismissAction: Boolean = false,
    val action: SnackbarAction? = null,
    val duration: SnackbarDuration = if (action == null && !withDismissAction) SnackbarDuration.Short else SnackbarDuration.Indefinite,
)

data class SnackbarAction(
    val name: UiText,
    val action: () -> Unit,
)

/**
 * Send events to create a message in the UI using the Snackbar API.
 *
 * Events can control the Snackbar using a [SnackbarEvent].
 *
 * # Examples:
 *
 * ```
 * class ScreenViewModel: ViewModel() {
 *     fun onError() = viewModelScope.launch {
 *          SnackbarController.sendEvent(SnackbarEvent("I am an error message"))
 *     }
 * }
 * ```
 */
object SnackbarController {
    private val _events = Channel<SnackbarEvent>()
    val events = _events.receiveAsFlow()

    suspend fun sendEvent(event: SnackbarEvent) {
        _events.send(event)
    }
}