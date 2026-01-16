package lol.terabrendon.houseshare2.util

import android.util.Log
import com.google.firebase.crashlytics.FirebaseCrashlytics
import lol.terabrendon.houseshare2.BuildConfig
import timber.log.Timber

object Logger {
    @JvmStatic
    fun setup(sendAnalytics: Boolean) {
        if (BuildConfig.DEBUG) {
            Timber.plant(DebugTree())
        } else if (sendAnalytics) {
            val firebase = FirebaseCrashlytics.getInstance()
            firebase.isCrashlyticsCollectionEnabled = true

            Timber.plant(CrashReportingTree())

            firebase.sendUnsentReports()
        }
    }

    class DebugTree : Timber.DebugTree() {
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
    }

    class CrashReportingTree : Timber.Tree() {
        override fun isLoggable(tag: String?, priority: Int): Boolean {
//            return priority == Log.WARN || priority == Log.ERROR
            return priority == Log.ERROR
        }

        override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
            val crashlytics = FirebaseCrashlytics.getInstance()

            crashlytics.log("$tag: $message")
            if (t != null) {
                crashlytics.recordException(t)
            }
        }
    }
}