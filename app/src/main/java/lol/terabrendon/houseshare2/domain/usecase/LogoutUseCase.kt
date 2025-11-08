package lol.terabrendon.houseshare2.domain.usecase

import android.content.Intent
import android.util.Log
import androidx.core.net.toUri
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import lol.terabrendon.houseshare2.data.remote.api.AuthApi
import lol.terabrendon.houseshare2.data.repository.UserDataRepository
import lol.terabrendon.houseshare2.presentation.navigation.MainNavigation
import lol.terabrendon.houseshare2.presentation.util.ActivityQueue
import lol.terabrendon.houseshare2.util.setQuery
import javax.inject.Inject

class LogoutUseCase @Inject constructor(
    private val userDataRepository: UserDataRepository,
    private val authApi: AuthApi,
) {
    companion object {
        private const val TAG = "LogoutUseCase"
    }

    // TODO: fix result error
    suspend operator fun invoke(): Result<Unit, String> {
        val res = authApi.logout()
        Log.i(TAG, "invoke: logout response: $res")

        if (res.code() !in 300..<400 || res.headers()["Location"] == null)
            return Err("")

        // TODO: Handle if the request is not a redirect
        val url = res.headers()["Location"]!!
        val uri = url.toUri()
            .setQuery("post_logout_redirect_uri", "app://lol.terabrendon.houseshare2/logout")
        val intent = Intent(Intent.ACTION_VIEW, uri)
        ActivityQueue.activities.emit(intent)

        userDataRepository.updateCurrentLoggedUser(null)
        userDataRepository.updateSelectedGroupId(null)
        userDataRepository.updateBackStack(listOf(MainNavigation.Login))

        Log.i(TAG, "invoke: logout successful!")

        return Ok(Unit)
    }
}