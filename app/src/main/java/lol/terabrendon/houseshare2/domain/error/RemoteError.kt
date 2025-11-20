package lol.terabrendon.houseshare2.domain.error

import retrofit2.Response

sealed interface RemoteError : DataError {
    data class Found(val response: Response<*>, val location: String) : RemoteError
    data class BadRequest(val response: Response<*>) : RemoteError
    data class Unauthorized(val response: Response<*>) : RemoteError
    data class Forbidden(val response: Response<*>) : RemoteError
    data class NotFound(val response: Response<*>) : RemoteError
    data class RequestTimeout(val response: Response<*>) : RemoteError
    data class ContentTooLarge(val response: Response<*>) : RemoteError
    data class UnsupportedMediaType(val response: Response<*>) : RemoteError
    data class TooManyRequests(val response: Response<*>) : RemoteError
    data class InternalServerError(val response: Response<*>) : RemoteError
    data class BadGateway(val response: Response<*>) : RemoteError
    data class ServiceUnavailable(val response: Response<*>) : RemoteError
    data class GatewayTimeout(val response: Response<*>) : RemoteError
    data class Unknown(val response: Response<*>) : RemoteError
    data object NoConnection : RemoteError
}