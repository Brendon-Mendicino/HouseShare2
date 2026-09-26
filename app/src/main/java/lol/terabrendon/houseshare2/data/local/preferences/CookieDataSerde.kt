package lol.terabrendon.houseshare2.data.local.preferences

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import lol.terabrendon.houseshare2.data.local.crypto.Crypto
import timber.log.Timber
import java.io.InputStream
import java.io.OutputStream
import kotlin.io.encoding.Base64

/**
 * The cipher is a parameter because the Android Keystore is not available in the jvm tests.
 */
class CookieDataSerializer(
    private val encrypt: (ByteArray) -> ByteArray = { Crypto.encrypt(it) },
    private val decrypt: (ByteArray) -> ByteArray = { Crypto.decrypt(it) },
) : Serializer<CookieData> {
    override val defaultValue: CookieData
        get() = CookieData()

    override suspend fun readFrom(input: InputStream): CookieData {
        val encryptedBytes = withContext(Dispatchers.IO) {
            input.readBytes()
        }

        return try {
            val decoded = Base64.decode(encryptedBytes)
            val decrypted = decrypt(decoded).decodeToString()
            Json.decodeFromString<CookieData>(decrypted)
        } catch (e: Exception) {
            // A lost key, a truncated file, a format change... Any of them means the cookies
            // cannot be read anymore.
            Timber.w(e, "Unable to read CookieData")
            throw CorruptionException("Unable to read CookieData", e)
        }
    }

    override suspend fun writeTo(t: CookieData, output: OutputStream) {
        val json = Json.encodeToString(t)
        val encrypted = encrypt(json.toByteArray())
        val encoded = Base64.encodeToByteArray(encrypted)

        withContext(Dispatchers.IO) {
            output.write(encoded)
        }
    }
}
