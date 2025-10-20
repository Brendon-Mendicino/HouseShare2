package lol.terabrendon.houseshare2.util

import android.net.Uri

fun Uri.setQuery(key: String, value: Any): Uri {
    val queryParams =
        queryParameterNames.associateTo(mutableMapOf()) { Pair(it!!, getQueryParameter(it)!!) }
    val newUri = buildUpon().clearQuery()
    queryParams[key] = value.toString()

    for ((k, v) in queryParams) {
        newUri.appendQueryParameter(k, v)
    }

    return newUri.build()!!
}

fun Uri.setScheme(scheme: String): Uri = buildUpon().scheme(scheme).build()

fun Uri.setAuthority(hostname: String, port: Int? = null): Uri =
    buildUpon().encodedAuthority(if (port != null) "$hostname:$port" else hostname).build()

fun Uri.setPath(path: String): Uri = buildUpon().encodedPath(path).build()