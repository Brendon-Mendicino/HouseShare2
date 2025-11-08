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

    override fun intercept(chain: Interceptor.Chain): Response {
        val oldRequest = chain.request()
        val builder = oldRequest.newBuilder()

        if (oldRequest.method in arrayOf("POST", "PUT", "DELETE", "PATCH")) {
            // Get the token if it's in the request
            val csrfToken = oldRequest
                .headers("Cookie")
                .joinToString(separator = ";")
                .split(";")
                .map { it.trim() }
                .filter { it.split("=").size == 2 }
                .firstNotNullOfOrNull {
                    val (key, value) = it.split("=")
                    if (key == "XSRF-TOKEN") value
                    else null
                }

            // Attach the token to the request headers.
            // This is needed by the server for the csrf protection.
            if (csrfToken != null) {
                builder.addHeader("X-XSRF-TOKEN", csrfToken)
            }
        }

        val request = builder.build()
        val response = chain.proceed(request)

        val csrfCookie = response
            .headers("Set-Cookie")
            .mapNotNull { Cookie.parse(request.url, it) }
            .firstOrNull { cookie -> cookie.name == "XSRF-TOKEN" && cookie.value.isNotEmpty() }

        if (csrfCookie != null) {
            Log.i(TAG, "intercept: new csrf intercepted.")
        }

        return response
    }
}