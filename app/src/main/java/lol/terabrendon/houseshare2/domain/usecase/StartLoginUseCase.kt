package lol.terabrendon.houseshare2.domain.usecase

import android.content.Intent
import androidx.core.net.toUri
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import lol.terabrendon.houseshare2.data.remote.api.LoginApi
import lol.terabrendon.houseshare2.presentation.util.ActivityQueue
import retrofit2.HttpException
import javax.inject.Inject

class StartLoginUseCase @Inject constructor(
    private val loginApi: LoginApi,
) {
    suspend operator fun invoke(): Result<Unit, HttpException> {
        val res = try {
            loginApi.login()
        } catch (e: HttpException) {
            return Err(e)
        }

        // TODO: Handle if the request is not a redirect
        val url = res.headers()["Location"]!!
        val uri = url.toUri()

        val intent = Intent(Intent.ACTION_VIEW, uri)
        ActivityQueue.activities.emit(intent)

        return Ok(Unit)
    }
}