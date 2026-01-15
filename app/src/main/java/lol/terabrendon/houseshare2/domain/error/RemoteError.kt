package lol.terabrendon.houseshare2.domain.error

import retrofit2.Response

sealed interface RemoteError : DataError {
    // 3XX
    data class Found(val response: Response<*>, val location: String) : RemoteError

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

    val is3xx: Boolean
        get() = this is Found

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