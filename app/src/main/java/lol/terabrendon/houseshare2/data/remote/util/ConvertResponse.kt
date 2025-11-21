package lol.terabrendon.houseshare2.data.remote.util

import android.util.Log
import androidx.core.net.toUri
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import lol.terabrendon.houseshare2.domain.error.RemoteError
import retrofit2.Response

private const val TAG = "ConvertResponse"

fun <T : Any> convertResponse(response: Response<T>): Result<T, RemoteError> {
    val code = response.code()
    val body = response.body()

    return if (response.isSuccessful && body != null) {
        Ok(body)
    } else {
        Log.w(
            TAG,
            "convertResponse: error caught while performing an http request. response=$response"
        )

        val error = when (code) {
            302 -> {
                val location = response.headers()["Location"]
                    ?: throw IllegalStateException("Response with code 302 must contain Location header!")

                if (location.toUri().path == "/login")
                    RemoteError.Unauthorized(response)
                else
                    RemoteError.Found(response, location)
            }

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