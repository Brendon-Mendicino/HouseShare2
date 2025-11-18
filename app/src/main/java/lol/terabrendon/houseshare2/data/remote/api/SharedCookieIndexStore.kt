package lol.terabrendon.houseshare2.data.remote.api

import android.content.SharedPreferences
import androidx.core.content.edit
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import lol.terabrendon.houseshare2.domain.kserializer.URIAsStr
import java.net.HttpCookie
import java.net.URI

/**
 * Never show this class to anyone...
 * This is a direct dependency of [SharedPrefCookieStore]
 */
class SharedCookieIndexStore(
    private val sharedPreferences: SharedPreferences,
) {
    @Serializable
    private data class CookieSerializable(
        val name: String,
        val value: String,
        val comment: String?,
        val discard: Boolean,
        val commentURL: String?,
        val domain: String?,
        val maxAge: Long,
        val path: String?,
        val portlist: String?,
        val isHttpOnly: Boolean,
        val version: Int,
    )

    @Serializable
    private data class CookieStore(
        val cookies: Map<URIAsStr, List<@Serializable(HttpCookieSerializer::class) HttpCookie>>,
    )

    object HttpCookieSerializer : KSerializer<HttpCookie> {
        override val descriptor: SerialDescriptor = CookieSerializable.serializer().descriptor

        override fun serialize(
            encoder: Encoder,
            value: HttpCookie,
        ) {
            val wrapper = CookieSerializable(
                name = value.name,
                value = value.value,
                domain = value.domain,
                path = value.path,
                maxAge = value.maxAge,
                comment = value.comment,
                discard = value.discard,
                commentURL = value.commentURL,
                portlist = value.portlist,
                isHttpOnly = value.isHttpOnly,
                version = value.version,
            )
            encoder.encodeSerializableValue(CookieSerializable.serializer(), wrapper)
        }

        override fun deserialize(decoder: Decoder): HttpCookie {
            val cookie = decoder.decodeSerializableValue(CookieSerializable.serializer())
            val httpCookie = HttpCookie(cookie.name, cookie.value).apply {
                domain = cookie.domain
                path = cookie.path
                maxAge = cookie.maxAge
                comment = cookie.comment
                discard = cookie.discard
                commentURL = cookie.commentURL
                portlist = cookie.portlist
                isHttpOnly = cookie.isHttpOnly
                version = cookie.version
            }

            return httpCookie
        }
    }

    fun get(): Map<URI, List<HttpCookie>> {
        val index = sharedPreferences.getString("cookie_index", null)
        if (index == null)
            return mutableMapOf()

        try {
            val map = Json.decodeFromString<CookieStore>(index)
            return map.cookies
        } catch (_: SerializationException) {
            return mutableMapOf()
        }
    }

    fun set(index: Map<URI, List<HttpCookie>>) {
        val json = Json.encodeToString(CookieStore(index))

        sharedPreferences.edit {
            putString("cookie_index", json)
        }
    }
}