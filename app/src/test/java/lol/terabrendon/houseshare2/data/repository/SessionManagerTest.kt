package lol.terabrendon.houseshare2.data.repository

import com.github.michaelbull.result.Err
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import lol.terabrendon.houseshare2.data.remote.api.AuthApi
import lol.terabrendon.houseshare2.domain.auth.Credentials
import lol.terabrendon.houseshare2.domain.error.RemoteError
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.wheneverBlocking
import java.net.ConnectException

class SessionManagerTest {

    private val authApi = mock<AuthApi>()
    private val sessionManager = SessionManager(authApi = authApi, idpAuthenticator = mock())

    private fun serverDown() {
        wheneverBlocking { authApi.login() }.thenAnswer { throw ConnectException("Connection refused") }
    }

    @Test
    fun `login with the server down returns no connection instead of throwing`() = runTest {
        serverDown()

        val res = sessionManager.login(Credentials(username = "giulia", password = "secret"))

        assertThat(res).isEqualTo(Err(RemoteError.NoConnection))
        assertThat(sessionManager.generation).isEqualTo(0)
    }

    @Test
    fun `renew with the server down returns no connection instead of throwing`() = runTest {
        serverDown()

        assertThat(sessionManager.renew(staleGeneration = 0)).isEqualTo(Err(RemoteError.NoConnection))
    }

    @Test
    fun `registration url with the server down returns no connection instead of throwing`() =
        runTest {
            serverDown()

            assertThat(sessionManager.registrationUrl()).isEqualTo(Err(RemoteError.NoConnection))
        }
}
