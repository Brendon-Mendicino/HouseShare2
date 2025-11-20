package lol.terabrendon.houseshare2.presentation.util

import android.content.Intent
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow

object ActivityQueue {
    private val intentChannel = Channel<Intent>()
    val intents = intentChannel.receiveAsFlow()

    suspend fun sendIntent(intent: Intent) {
        intentChannel.send(intent)
    }
}