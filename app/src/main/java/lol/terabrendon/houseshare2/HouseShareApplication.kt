package lol.terabrendon.houseshare2

import android.app.Application
import com.github.michaelbull.result.coroutines.runSuspendCatching
import com.github.michaelbull.result.onFailure
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import lol.terabrendon.houseshare2.data.repository.UserDataRepository
import lol.terabrendon.houseshare2.domain.error.RemoteError
import lol.terabrendon.houseshare2.domain.error.RootException
import lol.terabrendon.houseshare2.presentation.util.SnackbarController
import lol.terabrendon.houseshare2.util.Logger
import retrofit2.HttpException
import timber.log.Timber
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

@HiltAndroidApp
class HouseShareApplication : Application() {
    @Inject
    lateinit var userDataRepository: UserDataRepository

    override fun onCreate() {
        super.onCreate()

//        Composer.setDiagnosticStackTraceMode(ComposeStackTraceMode.Auto)

        applicationScope.launch {
            // Check first if we have the permissions to send analytics
            val sendAnalytics = userDataRepository.sendAnalytics.first()
            Logger.setup(sendAnalytics)
        }
    }

    private fun handleRoot(e: RootException) {
        val err = e.err

        applicationScope.launch {
            // Catch any possible exception to avoid creating circular calls to handleRoot
            runSuspendCatching {
                SnackbarController.sendError(err)
            }.onFailure { Timber.e(it, "sendError failed!") }
        }

        when (err) {
            is RemoteError.Unauthorized -> Timber.w(e, "Network request was made when logged-out.")
            else -> Timber.e(e, "Err was not handled inside the applicationScope.")
        }
    }

    private fun exceptionHandler(ctx: CoroutineContext, e: Throwable) {
        when (e) {
            is HttpException -> {
                Timber.e(
                    e, "CoroutineExceptionHandler: http exception: %s", e.response()?.toString()
                )
            }

            is RootException -> handleRoot(e)

            else -> {
                Timber.e(
                    e,
                    "Unhandled coroutine error. Catch by the applicationScope in ${HouseShareApplication::class.qualifiedName}."
                )
            }
        }
    }

    val applicationScope = CoroutineScope(
        SupervisorJob() + Dispatchers.Default + CoroutineName("AppScope") + CoroutineExceptionHandler(
            ::exceptionHandler
        )
    )
}