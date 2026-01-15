package lol.terabrendon.houseshare2

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import retrofit2.HttpException
import timber.log.Timber

@HiltAndroidApp
class HouseShareApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // TODO: setup a log-setup class
        if (BuildConfig.DEBUG) {
            Timber.plant(object : Timber.DebugTree() {
                override fun createStackElementTag(element: StackTraceElement) =
                    with(element) { "($fileName:$lineNumber)#$methodName()" }

                override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
                    super.log(
                        priority = priority,
                        tag = tag,
                        message = "%-50s | %s".format(tag, message),
                        t = t
                    )
                }
            })
        }
    }

    val applicationScope =
        CoroutineScope(SupervisorJob() + Dispatchers.Default + CoroutineName("AppScope") + CoroutineExceptionHandler { _, e ->
            if (e is HttpException) {
                Timber.e(
                    e, "CoroutineExceptionHandler: http exception: %s", e.response()?.toString()
                )
            } else {
                Timber.e(
                    e,
                    "Unhandled coroutine error. Catch by the applicationScope in ${HouseShareApplication::class.qualifiedName}."
                )
            }
        })
}