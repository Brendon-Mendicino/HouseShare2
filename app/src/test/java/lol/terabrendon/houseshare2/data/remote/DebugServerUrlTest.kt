package lol.terabrendon.houseshare2.data.remote

import com.google.common.truth.Truth.assertThat
import okhttp3.HttpUrl.Companion.toHttpUrl
import org.junit.After
import org.junit.Test

class DebugServerUrlTest {
    @After
    fun tearDown() = DebugServerUrl.set(null)

    @Test
    fun `rewrite leaves urls untouched with the default server`() {
        val url = DebugServerUrl.default.resolve("api/v1/users?page=1")!!

        assertThat(DebugServerUrl.rewrite(url)).isEqualTo(url)
    }

    @Test
    fun `rewrite moves the default server urls to the overridden one`() {
        DebugServerUrl.set(DebugServerUrl.parse("https://example.com:8443/prefix"))

        val url = DebugServerUrl.default.resolve("api/v1/users?page=1")!!

        assertThat(DebugServerUrl.rewrite(url))
            .isEqualTo("https://example.com:8443/prefix/api/v1/users?page=1".toHttpUrl())
    }

    @Test
    fun `rewrite leaves urls of other hosts untouched`() {
        DebugServerUrl.set(DebugServerUrl.parse("https://example.com/"))

        val url = "https://idp.other.org/realms/x/auth".toHttpUrl()

        assertThat(DebugServerUrl.rewrite(url)).isEqualTo(url)
    }

    @Test
    fun `parse adds the trailing slash and rejects garbage`() {
        assertThat(
            DebugServerUrl.parse(" http://10.0.2.2:9090 ").toString()
        ).isEqualTo("http://10.0.2.2:9090/")
        assertThat(DebugServerUrl.parse("not a url")).isNull()
    }
}
