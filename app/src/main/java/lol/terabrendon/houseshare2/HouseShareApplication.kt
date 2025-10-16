package lol.terabrendon.houseshare2

import android.app.Application
import android.util.Log
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

@HiltAndroidApp
class HouseShareApplication : Application() {
    val applicationScope =
        CoroutineScope(SupervisorJob() + Dispatchers.Default + CoroutineName("AppScope") + CoroutineExceptionHandler { _, e ->
            Log.e(
                "AppScope",
                "Unhandled coroutine error. Catch by the applicationScope in ${HouseShareApplication::class.qualifiedName}.",
                e
            )
        })
}