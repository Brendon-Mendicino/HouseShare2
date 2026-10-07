package lol.terabrendon.houseshare2.data.remote

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import lol.terabrendon.houseshare2.BuildConfig
import lol.terabrendon.houseshare2.data.remote.DebugServerUrl.current
import lol.terabrendon.houseshare2.data.remote.DebugServerUrl.default
import lol.terabrendon.houseshare2.data.remote.DebugServerUrl.init
import lol.terabrendon.houseshare2.data.remote.DebugServerUrl.rewrite
import lol.terabrendon.houseshare2.data.remote.DebugServerUrl.set
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import timber.log.Timber

/**
 * The url of the server the app talks to.
 *
 * Retrofit clients are built once with [default] ([BuildConfig.BASE_URL]). In debug builds [set]
 * can point the app to another server at runtime: [current] holds the override and
 * [lol.terabrendon.houseshare2.data.remote.interceptor.BaseUrlInterceptor] moves every request
 * from [default] to it through [rewrite]. Release builds always use [default].
 *
 * The interceptor reads the override synchronously, so it is kept in memory and observed by the
 * settings screen through [current]. It survives restarts in a [SharedPreferences] file, which
 * unlike the DataStore can be read synchronously by [init] before any request is made.
 */
object DebugServerUrl {
    val default: HttpUrl = BuildConfig.BASE_URL.toHttpUrl()

    private val _current = MutableStateFlow(default)
    val current: StateFlow<HttpUrl> = _current.asStateFlow()

    private const val KEY = "server_url"
    private var prefs: SharedPreferences? = null

    /**
     * Restores the stored override, debug builds only.
     */
    fun init(context: Context) {
        if (!BuildConfig.DEBUG) return

        val prefs = context.getSharedPreferences("debug_server_url", Context.MODE_PRIVATE)
        this.prefs = prefs

        prefs.getString(KEY, null)?.let(::parse)?.let { url ->
            Timber.i("init: restored server url %s", url)
            _current.value = url
        }
    }

    /**
     * Parses a user typed url, the trailing slash is added when missing, as Retrofit requires it.
     */
    fun parse(input: String): HttpUrl? = input
        .trim()
        .let { if (it.endsWith('/')) it else "$it/" }
        .toHttpUrlOrNull()

    /**
     * Overrides the server url, `null` restores [default]. Only allowed in debug builds.
     */
    fun set(url: HttpUrl?) {
        check(BuildConfig.DEBUG) { "The server url can only be changed in debug builds" }

        Timber.i("set: server url changed to %s", url ?: default)
        _current.value = url ?: default
        prefs?.edit { if (url == null) remove(KEY) else putString(KEY, url.toString()) }
    }

    /**
     * Moves a url of the [default] server to the [current] one, any other url is left as it is.
     */
    fun rewrite(url: HttpUrl): HttpUrl {
        val target = _current.value
        if (target == default) return url

        val sameServer =
            url.scheme == default.scheme && url.host == default.host && url.port == default.port
        if (!sameServer || !url.encodedPath.startsWith(default.encodedPath)) return url

        return url.newBuilder()
            .scheme(target.scheme)
            .host(target.host)
            .port(target.port)
            .encodedPath(target.encodedPath + url.encodedPath.removePrefix(default.encodedPath))
            .build()
    }
}
