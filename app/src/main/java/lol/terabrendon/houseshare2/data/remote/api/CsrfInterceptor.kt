package lol.terabrendon.houseshare2.data.remote.api

import android.util.Log
import okhttp3.Cookie
import okhttp3.Interceptor
import okhttp3.Response

/**
 * HTTP interceptors for csrf tokens. Automatically handles getting
 * the tokens from the responses, and sets them in the subsequent
 * requests.
 */
class CsrfInterceptor : Interceptor {
    companion object {
        private const val TAG = "CsrfInterceptor"
    }

    private var csrfToken: String? = null


    override fun intercept(chain: Interceptor.Chain): Response {
        val oldRequest = chain.request()
        val builder = oldRequest.newBuilder()

        if (oldRequest.method in listOf("POST", "PUT", "DELETE", "PATCH")) {
            csrfToken?.let { builder.addHeader("X-XSRF-TOKEN", it) }
        }

        val request = builder.build()
        val response = chain.proceed(request)

        val csrfCookie = response
            .headers("Set-Cookie")
            .mapNotNull { Cookie.parse(request.url, it) }
            .firstOrNull { cookie -> cookie.name == "XSRF-TOKEN" && cookie.value.isNotEmpty() }

        if (csrfCookie != null) {
            csrfToken = csrfCookie.value
            println(csrfToken)
            println(csrfCookie)
            Log.i(TAG, "intercept: new csrf intercepted.")
        }

        return response
    }
}