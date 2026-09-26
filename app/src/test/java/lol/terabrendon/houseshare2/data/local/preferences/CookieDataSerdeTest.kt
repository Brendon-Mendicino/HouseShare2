package lol.terabrendon.houseshare2.data.local.preferences

import androidx.datastore.core.CorruptionException
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.runBlocking
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.net.HttpCookie
import java.net.URI
import javax.crypto.BadPaddingException

class CookieDataSerdeTest {
    /**
     * The real cipher uses the Android Keystore, which is not available on the jvm. What is tested
     * here is the serializer, not the cipher.
     */
    private val serializer = CookieDataSerializer(
        encrypt = { it.reversedArray() },
        decrypt = { it.reversedArray() },
    )

    /** What a lost Keystore key looks like. */
    private val brokenSerializer = CookieDataSerializer(
        encrypt = { it },
        decrypt = { throw BadPaddingException() },
    )

    private val session = HttpCookie("JSESSIONID", "super-secret-session").apply {
        path = "/"
        isHttpOnly = true
        maxAge = 3600
    }

    private val data = CookieData(
        cookies = mapOf(URI.create("http://houseshare.example") to listOf(session.toStored())),
    )

    private fun CookieDataSerializer.write(data: CookieData): ByteArray = runBlocking {
        ByteArrayOutputStream().also { writeTo(data, it) }.toByteArray()
    }

    private fun CookieDataSerializer.read(bytes: ByteArray): CookieData = runBlocking {
        readFrom(ByteArrayInputStream(bytes))
    }

    @Test
    fun `cookies survive a write and a read`() {
        assertThat(serializer.read(serializer.write(data))).isEqualTo(data)
    }

    @Test
    fun `the stored bytes are not readable`() {
        assertThat(serializer.write(data).decodeToString()).doesNotContain("super-secret-session")
    }

    @Test(expected = CorruptionException::class)
    fun `bytes that cannot be decrypted are reported as corruption`() {
        brokenSerializer.read(serializer.write(data))
    }

    @Test(expected = CorruptionException::class)
    fun `bytes that are not cookies are reported as corruption`() {
        serializer.read("not cookies".toByteArray().reversedArray())
    }

    @Test
    fun `a cookie keeps its attributes`() {
        val restored = session.toStored().toHttpCookie()

        assertThat(restored.toStored()).isEqualTo(session.toStored())
    }

    @Test
    fun `a cookie whose value changed is a different cookie`() {
        // HttpCookie considers them equal, the DataStore would not write the new one.
        val renewed = HttpCookie("JSESSIONID", "another-session").apply { path = "/" }
        val old = HttpCookie("JSESSIONID", "old-session").apply { path = "/" }

        assertThat(renewed).isEqualTo(old)
        assertThat(renewed.toStored()).isNotEqualTo(old.toStored())
    }
}
