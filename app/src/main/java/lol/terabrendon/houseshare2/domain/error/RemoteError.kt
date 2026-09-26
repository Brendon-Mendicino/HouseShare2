@file:Suppress("unused")

package lol.terabrendon.houseshare2.domain.error

import retrofit2.Response

sealed interface RemoteError : DataError {
    // 3XX
    data class Redirect(val response: Response<*>, val location: String) : RemoteError

    // 4XX
    data class BadRequest(val response: Response<*>) : RemoteError
    data class Unauthorized(val response: Response<*>) : RemoteError
    data class Forbidden(val response: Response<*>) : RemoteError
    data class NotFound(val response: Response<*>) : RemoteError
    data class RequestTimeout(val response: Response<*>) : RemoteError
    data class ContentTooLarge(val response: Response<*>) : RemoteError
    data class UnsupportedMediaType(val response: Response<*>) : RemoteError
    data class TooManyRequests(val response: Response<*>) : RemoteError

    // 5XX
    data class InternalServerError(val response: Response<*>) : RemoteError
    data class BadGateway(val response: Response<*>) : RemoteError
    data class ServiceUnavailable(val response: Response<*>) : RemoteError
    data class GatewayTimeout(val response: Response<*>) : RemoteError

    // Other
    data class Unknown(val response: Response<*>) : RemoteError
    data object NoConnection : RemoteError

    /**
     * There is no session and none could be obtained: either no token is stored on the device or
     * the refresh token was rejected by the OIDC provider. The user has to log in again.
     */
    data object NoSession : RemoteError

    /**
     * The identity provider refused the credentials. [reason] is the message it answered with,
     * when it gave one.
     */
    data class InvalidCredentials(val reason: String? = null) : RemoteError

    fun maybeResponse() = when (this) {
        is BadGateway -> this.response
        is BadRequest -> this.response
        is ContentTooLarge -> this.response
        is Forbidden -> this.response
        is GatewayTimeout -> this.response
        is InternalServerError -> this.response
        is NotFound -> this.response
        is Redirect -> this.response
        is RequestTimeout -> this.response
        is ServiceUnavailable -> this.response
        is TooManyRequests -> this.response
        is Unauthorized -> this.response
        is Unknown -> this.response
        is UnsupportedMediaType -> this.response
        is NoConnection -> null
        is NoSession -> null
        is InvalidCredentials -> null
    }

    fun message(): String {
        if (this is NoSession) {
            return "No session"
        }

        if (this is InvalidCredentials) {
            return reason ?: "Invalid credentials"
        }

        val response = maybeResponse()
        if (response == null) {
            return "No connection"
        }

        return "[$response.code()] ${response.message() ?: "Unknown"}"
    }

    val is3xx: Boolean
        get() = this is Redirect

    val is4xx: Boolean
        get() = when (this) {
            is BadRequest,
            is Unauthorized,
            is Forbidden,
            is NotFound,
            is RequestTimeout,
            is ContentTooLarge,
            is UnsupportedMediaType,
            is TooManyRequests,
                // Not status codes, but they mean the same thing to every caller: logged out.
            is NoSession,
            is InvalidCredentials,
                -> true

            else -> false
        }

    val is5xx: Boolean
        get() = when (this) {
            is InternalServerError,
            is BadGateway,
            is ServiceUnavailable,
            is GatewayTimeout,
                -> true

            else -> false
        }
}