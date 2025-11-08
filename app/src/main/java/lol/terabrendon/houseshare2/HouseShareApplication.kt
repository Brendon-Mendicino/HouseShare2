package lol.terabrendon.houseshare2

import android.app.Application
import android.util.Log
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import retrofit2.HttpException

@HiltAndroidApp
class HouseShareApplication : Application() {
    companion object {
        private const val TAG = "HouseShareApplication"
    }

    val applicationScope =
        CoroutineScope(SupervisorJob() + Dispatchers.Default + CoroutineName("AppScope") + CoroutineExceptionHandler { _, e ->
            if (e is HttpException) {
                Log.e(
                    TAG,
                    "CoroutineExceptionHandler: http exception: ${e.response()?.toString()}",
                    e
                )
            } else {
                Log.e(
                    TAG,
                    "Unhandled coroutine error. Catch by the applicationScope in ${HouseShareApplication::class.qualifiedName}.",
                    e
                )
            }
        })
}