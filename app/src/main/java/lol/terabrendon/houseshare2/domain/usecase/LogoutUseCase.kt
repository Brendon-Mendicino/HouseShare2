package lol.terabrendon.houseshare2.domain.usecase

import android.content.Intent
import android.util.Log
import androidx.core.net.toUri
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.coroutineBinding
import com.github.michaelbull.result.getErrorOr
import lol.terabrendon.houseshare2.data.remote.api.AuthApi
import lol.terabrendon.houseshare2.data.repository.UserDataRepository
import lol.terabrendon.houseshare2.domain.error.DataError
import lol.terabrendon.houseshare2.domain.error.RemoteError
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

    suspend operator fun invoke(): Result<Unit, DataError> = coroutineBinding {
        val res = authApi.logout()
        Log.i(TAG, "invoke: logout response: $res")

        val redirect = res.getErrorOr(null)
        val uri = when (redirect) {
            is RemoteError.Found -> redirect.location.toUri()
            else -> null
        }

        if (uri != null) {
            val logoutUri = uri
                .setQuery("post_logout_redirect_uri", "app://lol.terabrendon.houseshare2/logout")

            val intent = Intent(Intent.ACTION_VIEW, logoutUri)
            ActivityQueue.sendIntent(intent)
            Log.i(TAG, "invoke: logout successful!")
        } else {
            Log.w(TAG, "invoke: logout failed! response=$res")
        }

        userDataRepository.updateCurrentLoggedUser(null)
        userDataRepository.updateSelectedGroupId(null)
        userDataRepository.updateBackStack(listOf(MainNavigation.Login))
    }
}