package lol.terabrendon.houseshare2.domain.usecase

import android.content.Intent
import androidx.core.net.toUri
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.unwrapError
import lol.terabrendon.houseshare2.data.remote.api.AuthApi
import lol.terabrendon.houseshare2.data.remote.util.convertResponse
import lol.terabrendon.houseshare2.domain.error.RemoteError
import lol.terabrendon.houseshare2.presentation.util.ActivityQueue
import timber.log.Timber
import javax.inject.Inject

class StartLoginUseCase @Inject constructor(
    private val authApi: AuthApi,
) {
    suspend operator fun invoke(): Result<Unit, RemoteError> {
        val res = authApi.login()

        if (res.isSuccessful) {
            Timber.e("invoke: login api return success. They must return a redirect.")
            return Err(RemoteError.Unknown(res))
        }

        val err = convertResponse(res).unwrapError()
        if (err !is RemoteError.Redirect) {
            return Err(err)
        }

        val redirect = err.location.toUri()

        val intent = Intent(Intent.ACTION_VIEW, redirect)
        ActivityQueue.sendIntent(intent)

        return Ok(Unit)
    }
}