package lol.terabrendon.houseshare2.data.remote.util

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import lol.terabrendon.houseshare2.domain.error.RemoteError
import retrofit2.Response

fun <T : Any> convertResponse(response: Response<T>): Result<T, RemoteError> {
    val code = response.code()
    val body = response.body()

    return if (response.isSuccessful && body != null) {
        Ok(body)
    } else {
        val error = when (code) {
            302 -> RemoteError.Found(response,
                response.headers()["Location"]
                    ?: throw IllegalStateException("Response must contain Location header")
            )

            400 -> RemoteError.BadRequest(response)
            401 -> RemoteError.Unauthorized(response)
            403 -> RemoteError.Forbidden(response)
            404 -> RemoteError.NotFound(response)
            408 -> RemoteError.RequestTimeout(response)
            413 -> RemoteError.ContentTooLarge(response)
            415 -> RemoteError.UnsupportedMediaType(response)
            429 -> RemoteError.TooManyRequests(response)
            500 -> RemoteError.InternalServerError(response)
            502 -> RemoteError.BadGateway(response)
            503 -> RemoteError.ServiceUnavailable(response)
            504 -> RemoteError.GatewayTimeout(response)
            else -> RemoteError.Unknown(response)
        }

        Err(error)
    }
}