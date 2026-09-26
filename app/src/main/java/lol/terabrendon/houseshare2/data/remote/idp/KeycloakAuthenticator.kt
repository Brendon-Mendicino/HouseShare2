package lol.terabrendon.houseshare2.data.remote.idp

import androidx.core.net.toUri
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.unwrapError
import lol.terabrendon.houseshare2.data.remote.api.IdpApi
import lol.terabrendon.houseshare2.data.remote.api.NetResult
import lol.terabrendon.houseshare2.data.remote.util.convertResponse
import lol.terabrendon.houseshare2.domain.auth.Credentials
import lol.terabrendon.houseshare2.domain.auth.IdpAuthenticator
import lol.terabrendon.houseshare2.domain.error.RemoteError
import okhttp3.ResponseBody
import retrofit2.Response
import timber.log.Timber
import javax.inject.Inject

/**
 * [IdpAuthenticator] for Keycloak: it walks the same pages a browser would, posting the
 * credentials to the login form of the provider.
 *
 * Nothing about the request is hardcoded: the authorization url comes from the server, and the
 * fields to post come from the page itself. What is Keycloak specific, and is the reason this
 * class exists, is how the answers are read: the markup of the login page, and the fact that
 * refused credentials are answered with the login page again instead of an error status.
 */
class KeycloakAuthenticator @Inject constructor(
    private val idpApi: IdpApi,
) : IdpAuthenticator {
    companion object {
        /** A login is a handful of redirects, anything longer is a loop. */
        private const val MAX_HOPS = 5
    }

    override suspend fun authenticate(
        authorizationUrl: String,
        credentials: Credentials?,
    ): NetResult<String> {
        // The url the provider has to send the code to. It tells the redirects that end the flow
        // apart from the ones internal to the provider.
        val redirectUri = authorizationUrl.toUri().getQueryParameter("redirect_uri")
        if (redirectUri == null) {
            Timber.e("authenticate: the authorization url carries no redirect_uri")
            return Err(RemoteError.NoSession)
        }

        val page = when (val hop = follow(authorizationUrl, redirectUri)) {
            // With a provider session still alive there is nothing to ask: the code comes back.
            is Hop.Code -> {
                Timber.i("authenticate: the provider session is still valid")
                return Ok(hop.redirect)
            }

            is Hop.Page -> hop.response
        }

        if (!page.isSuccessful) {
            Timber.w("authenticate: the provider refused the authorization request")
            return Err(convertResponse(page).unwrapError())
        }

        if (credentials == null) {
            // A silent authentication: the provider is asking to log in again, but there is
            // nobody to type the password.
            Timber.i("authenticate: the provider asks for the credentials")
            return Err(RemoteError.NoSession)
        }

        val form = page.body()?.string()?.let { KeycloakLoginPage.parse(it) }
        if (form == null) {
            Timber.e("authenticate: the provider did not answer with a login page")
            return Err(RemoteError.Unknown(page))
        }

        val answer = idpApi.post(
            url = form.action,
            fields = buildMap {
                // Whatever the page carries is posted back, without knowing what it means.
                putAll(form.fields)
                put("username", credentials.username)
                put("password", credentials.password)

                // A phone is not a shared browser: asking to be remembered is what keeps the
                // provider session alive between two uses of the app.
                if (form.rememberMe) put("rememberMe", "on")
            },
        )

        val result = when (val hop = answer.next(redirectUri)) {
            is Hop.Code -> return Ok(hop.redirect).also {
                Timber.i("authenticate: the provider accepted the credentials")
            }

            is Hop.Page -> hop.response
        }

        if (result.isSuccessful) {
            // Keycloak answers 200 with the login page again, the reason is written on it.
            val reason = result.body()?.string()?.let { KeycloakLoginPage.errorMessage(it) }
            Timber.w("authenticate: the credentials were refused, reason=%s", reason)

            return Err(RemoteError.InvalidCredentials(reason))
        }

        Timber.w("authenticate: unexpected answer from the provider, code=%d", result.code())

        return Err(convertResponse(result).unwrapError())
    }

    private sealed interface Hop {
        /** The flow is over, [redirect] carries the authorization code. */
        data class Code(val redirect: String) : Hop

        /** The provider answered with something to look at, or with an error. */
        data class Page(val response: Response<ResponseBody>) : Hop
    }

    /**
     * Walks [url] until the provider hands back the code or answers with a page. The provider can
     * redirect inside itself before getting to the point, the same way it does in a browser.
     */
    private suspend fun follow(url: String, redirectUri: String): Hop {
        var response = idpApi.get(url)

        repeat(MAX_HOPS) {
            when (val hop = response.next(redirectUri)) {
                is Hop.Code -> return hop
                is Hop.Page -> {
                    val location = response.internalRedirect() ?: return hop
                    Timber.i("follow: following a redirect internal to the provider")
                    response = idpApi.get(location)
                }
            }
        }

        Timber.w("follow: too many redirects")

        return Hop.Page(response)
    }

    private fun Response<ResponseBody>.next(redirectUri: String): Hop {
        val location = headers()["Location"]?.takeIf { code() in 300..399 }

        return if (location != null && location.startsWith(redirectUri)) Hop.Code(location)
        else Hop.Page(this)
    }

    /**
     * The absolute url of a redirect that is not the end of the flow, or `null` when this response
     * is not a redirect.
     */
    private fun Response<ResponseBody>.internalRedirect(): String? {
        if (code() !in 300..399) return null

        val location = headers()["Location"] ?: return null

        // A Location can be relative, it is resolved against the url that produced it.
        return raw().request.url.resolve(location)?.toString()
    }
}
