package lol.terabrendon.houseshare2.data.remote.idp

import com.google.common.truth.Truth.assertThat
import okhttp3.HttpUrl.Companion.toHttpUrl
import org.junit.Test
import org.mockito.kotlin.mock

class KeycloakAuthenticatorTest {
    private val authenticator = KeycloakAuthenticator(idpApi = mock())

    private val authorizationUrl =
        "http://192.168.1.150:8080/realms/house-share/protocol/openid-connect/auth" +
                "?response_type=code&client_id=house-share-app&scope=openid%20profile" +
                "&state=abc%3D&redirect_uri=http://192.168.1.150:9090/login/oauth2" +
                "&code_challenge=xyz&code_challenge_method=S256"

    @Test
    fun `registration url asks the provider to create an account`() {
        val url = authenticator.registrationUrl(authorizationUrl).toHttpUrl()

        assertThat(url.queryParameter("prompt")).isEqualTo("create")
    }

    @Test
    fun `registration url keeps the authorization request as it is`() {
        val original = authorizationUrl.toHttpUrl()
        val url = authenticator.registrationUrl(authorizationUrl).toHttpUrl()

        assertThat(url.encodedPath).isEqualTo(original.encodedPath)
        original.queryParameterNames.forEach { name ->
            assertThat(url.queryParameter(name)).isEqualTo(original.queryParameter(name))
        }
    }

    @Test
    fun `registration url replaces a prompt already present`() {
        val url = authenticator.registrationUrl("$authorizationUrl&prompt=login").toHttpUrl()

        assertThat(url.queryParameterValues("prompt")).containsExactly("create")
    }
}
