package lol.terabrendon.houseshare2.domain.usecase

import android.content.Intent
import androidx.core.net.toUri
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.unwrapError
import lol.terabrendon.houseshare2.data.remote.api.AuthApi
import lol.terabrendon.houseshare2.domain.error.RemoteError
import lol.terabrendon.houseshare2.presentation.util.ActivityQueue
import retrofit2.HttpException
import javax.inject.Inject

class StartLoginUseCase @Inject constructor(
    private val authApi: AuthApi,
) {
    suspend operator fun invoke(): Result<Unit, HttpException> {
        val res = authApi.login()
        println(res)

        // TODO: Handle if the request is not a redirect
        val redirect = (res.unwrapError() as RemoteError.Found).location.toUri()

        val intent = Intent(Intent.ACTION_VIEW, redirect)
        ActivityQueue.sendIntent(intent)

        return Ok(Unit)
    }
}