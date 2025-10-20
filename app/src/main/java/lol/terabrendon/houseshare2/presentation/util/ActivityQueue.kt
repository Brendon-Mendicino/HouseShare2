package lol.terabrendon.houseshare2.presentation.util

import android.content.Intent
import kotlinx.coroutines.flow.MutableSharedFlow

// TODO: remove this, find a better solution, or if this is good enough move to different package
object ActivityQueue {
    val activities = MutableSharedFlow<Intent>()
}