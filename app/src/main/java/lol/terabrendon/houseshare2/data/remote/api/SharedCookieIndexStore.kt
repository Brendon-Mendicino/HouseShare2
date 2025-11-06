package lol.terabrendon.houseshare2.data.remote.api

import android.content.SharedPreferences
import androidx.core.content.edit
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializer
import com.google.gson.JsonObject
import com.google.gson.JsonSerializer
import com.google.gson.reflect.TypeToken
import java.net.HttpCookie
import java.net.URI

/**
 * Never show this class to anyone...
 * This is a direct dependency of [SharedPrefCookieStore]
 */
class SharedCookieIndexStore(
    private val sharedPreferences: SharedPreferences
) {
    private val token = object : TypeToken<Map<URI, List<HttpCookie>>>() {}
    private val gson = GsonBuilder()
        .registerTypeAdapter(
            HttpCookie::class.java,
            JsonSerializer<HttpCookie> { cookie, type, context ->
                val json = JsonObject()
                json.addProperty("name", cookie.name)
                json.addProperty("value", cookie.value)
                json.addProperty("comment", cookie.comment)
                json.addProperty("toDiscard", cookie.discard)
                json.addProperty("commentURL", cookie.commentURL)
                json.addProperty("domain", cookie.domain)
                json.addProperty("maxAge", cookie.maxAge)
                json.addProperty("path", cookie.path)
                json.addProperty("portlist", cookie.portlist)
                json.addProperty("httpOnly", cookie.isHttpOnly)
                json.addProperty("version", cookie.version)
                json.addProperty(
                    "whenCreated",
                    cookie::class.java.getDeclaredField("whenCreated").apply { isAccessible = true }
                        .get(cookie)?.toString()
                )

                json
            })
        .registerTypeAdapter(
            HttpCookie::class.java,
            JsonDeserializer<HttpCookie> { json, type, context ->
                val get =
                    { s: String -> if (json.asJsonObject.has(s)) json.asJsonObject.get(s) else null }

                val cookie = HttpCookie(get("name")?.asString, get("value")?.asString)

                cookie.comment = get("comment")?.asString
                get("toDiscard")?.let { cookie.discard = it.asBoolean }
                cookie.commentURL = get("commentURL")?.asString
                cookie.domain = get("domain")?.asString
                get("maxAge")?.let { cookie.maxAge = it.asLong }
                cookie.path = get("path")?.asString
                cookie.portlist = get("portlist")?.asString
                get("httpOnly")?.let { cookie.isHttpOnly = it.asBoolean }
                get("version")?.let { cookie.version = it.asInt }
                get("whenCreated")?.let {
                    cookie::class.java.getDeclaredField("whenCreated").apply { isAccessible = true }
                        .set(cookie, it.asLong)
                }

                cookie
            })
        .create()


    fun get(): Map<URI, List<HttpCookie>> {
        val index = sharedPreferences.getString("cookie_index", null)
        if (index == null)
            return mutableMapOf()

        try {
            val map = gson.fromJson(index, token)
            return map
        } catch (_: Throwable) {
            return mutableMapOf()
        }
    }

    fun set(index: Map<URI, List<HttpCookie>>) {
        val json = gson.toJson(index, token.type)

        sharedPreferences.edit {
            putString("cookie_index", json)
        }
    }
}