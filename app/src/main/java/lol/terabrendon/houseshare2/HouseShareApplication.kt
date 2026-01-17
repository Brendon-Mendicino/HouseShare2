package lol.terabrendon.houseshare2

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import lol.terabrendon.houseshare2.data.repository.UserDataRepository
import lol.terabrendon.houseshare2.util.Logger
import retrofit2.HttpException
import timber.log.Timber
import javax.inject.Inject

@HiltAndroidApp
class HouseShareApplication : Application() {
    @Inject
    lateinit var userDataRepository: UserDataRepository

    override fun onCreate() {
        super.onCreate()

        // TODO: enable when having KOTLIN_2.3.0
//        Composer.setDiagnosticStackTraceMode(ComposeStackTraceMode.Auto)

        applicationScope.launch {
            val sendAnalytics = userDataRepository.sendAnalytics.first()
            Logger.setup(sendAnalytics)
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