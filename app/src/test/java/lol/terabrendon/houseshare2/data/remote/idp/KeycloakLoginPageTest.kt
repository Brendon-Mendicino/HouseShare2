package lol.terabrendon.houseshare2.data.remote.idp

import com.google.common.truth.Truth.assertThat
import org.junit.Test

/**
 * The fixtures are real pages, as served by Keycloak 24 for the `house-share` realm.
 */
class KeycloakLoginPageTest {
    private fun page(name: String): String = javaClass.classLoader!!
        .getResourceAsStream(name)!!
        .use { it.readBytes().decodeToString() }

    private val loginPage = page("keycloak_login_page.html")
    private val errorPage = page("keycloak_login_error_page.html")

    @Test
    fun `the action is read and unescaped`() {
        val form = KeycloakLoginPage.parse(loginPage)

        assertThat(form).isNotNull()
        assertThat(form!!.action).startsWith("http://192.168.1.150:8080/realms/house-share/login-actions/authenticate?")
        // The markup escapes the query separators, posting to them as they are would break.
        assertThat(form.action).doesNotContain("&amp;")
        assertThat(form.action).contains("session_code=")
        assertThat(form.action).contains("execution=")
        assertThat(form.action).contains("tab_id=")
    }

    @Test
    fun `the fields the page carries are collected`() {
        val form = KeycloakLoginPage.parse(loginPage)!!

        // Whatever the provider puts in the form is posted back, without knowing what it means.
        assertThat(form.fields).containsKey("credentialId")
    }

    @Test
    fun `the remember me option is detected`() {
        assertThat(KeycloakLoginPage.parse(loginPage)!!.rememberMe).isTrue()
        assertThat(
            KeycloakLoginPage.parse(
                loginPage.replace(
                    "rememberMe",
                    "somethingElse"
                )
            )!!.rememberMe
        )
            .isFalse()
    }

    @Test
    fun `a page without a login form is not one`() {
        assertThat(KeycloakLoginPage.parse("<html><body>Nothing here</body></html>")).isNull()
    }

    @Test
    fun `the refusal message is read`() {
        assertThat(KeycloakLoginPage.errorMessage(errorPage))
            .isEqualTo("Invalid username or password.")
    }

    @Test
    fun `a page without a message has none`() {
        assertThat(KeycloakLoginPage.errorMessage(loginPage)).isNull()
    }

    @Test
    fun `the refused page is still a login form, so it can be told apart from a success`() {
        assertThat(KeycloakLoginPage.parse(errorPage)).isNotNull()
    }
}
