package lol.terabrendon.houseshare2.data.local.preferences

import kotlinx.serialization.Serializable
import lol.terabrendon.houseshare2.domain.kserializer.URIAsStr
import java.net.HttpCookie

/**
 * The cookies of the app indexed by uri, as kept by
 * [lol.terabrendon.houseshare2.data.remote.api.SharedPrefCookieStore].
 *
 * They hold the sessions of the server and of the identity provider, which are as good as the
 * credentials of the user: the file is encrypted in release builds, see [cookiePreferencesStore].
 */
@Serializable
data class CookieData(
    val cookies: Map<URIAsStr, List<StoredCookie>> = emptyMap(),
)

/**
 * [HttpCookie.equals] only compares name, domain and path. The DataStore does not write a value
 * equal to the current one, so storing [HttpCookie]s directly would lose every cookie whose value
 * changed, e.g. a renewed session.
 */
@Serializable
data class StoredCookie(
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

fun HttpCookie.toStored() = StoredCookie(
    name = name,
    value = value,
    comment = comment,
    discard = discard,
    commentURL = commentURL,
    domain = domain,
    maxAge = maxAge,
    path = path,
    portlist = portlist,
    isHttpOnly = isHttpOnly,
    version = version,
)

fun StoredCookie.toHttpCookie() = HttpCookie(name, value).also {
    it.comment = comment
    it.discard = discard
    it.commentURL = commentURL
    it.domain = domain
    it.maxAge = maxAge
    it.path = path
    it.portlist = portlist
    it.isHttpOnly = isHttpOnly
    it.version = version
}
