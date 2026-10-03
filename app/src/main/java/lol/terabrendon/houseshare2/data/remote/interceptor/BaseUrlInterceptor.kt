package lol.terabrendon.houseshare2.data.remote.interceptor

import lol.terabrendon.houseshare2.data.remote.DebugServerUrl
import okhttp3.Interceptor
import okhttp3.Response

/**
 * Sends the requests to the server chosen through [DebugServerUrl], the Retrofit clients are all built
 * with the default one.
 *
 * It must be an application interceptor added before the others: the cookie jar and the network
 * interceptors then see the rewritten url.
 */
class BaseUrlInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val url = DebugServerUrl.rewrite(request.url)

        if (url == request.url) {
            return chain.proceed(request)
        }

        return chain.proceed(request.newBuilder().url(url).build())
    }
}
