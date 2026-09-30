package lol.terabrendon.houseshare2.domain.auth

import lol.terabrendon.houseshare2.data.remote.api.NetResult

/**
 * The credentials typed by the user. They are never stored: what survives the login is the
 * session of the identity provider, kept in the cookie jar.
 */
data class Credentials(
    val username: String,
    val password: String,
)

/**
 * Performs the user authentication step of the OAuth2 code flow against the identity provider,
 * without leaving the app.
 *
 * The rest of the flow stays where it was: the server builds the authorization request and
 * exchanges the code, this interface only covers the part that used to require a browser. Every
 * detail of how the provider asks for credentials belongs to the implementation, so that changing
 * provider means writing a new one and changing a single binding.
 */
interface IdpAuthenticator {
    /**
     * Follows [authorizationUrl] and, when the provider asks for them, sends [credentials].
     *
     * Returns the url the provider redirects to, the one carrying the authorization code, to be
     * handed back to the server.
     *
     * When [credentials] is `null` this is a silent authentication: it succeeds only if the
     * provider still recognises the session stored in the cookie jar, otherwise it fails with
     * [lol.terabrendon.houseshare2.domain.error.RemoteError.NoSession].
     */
    suspend fun authenticate(
        authorizationUrl: String,
        credentials: Credentials?,
    ): NetResult<String>

    /**
     * The url of the provider page where a new user signs up, derived from [authorizationUrl].
     *
     * Registration is the one step the app does not perform itself: the url is opened in a
     * browser, and once the account exists the user comes back and logs in with [authenticate].
     */
    fun registrationUrl(authorizationUrl: String): String
}
