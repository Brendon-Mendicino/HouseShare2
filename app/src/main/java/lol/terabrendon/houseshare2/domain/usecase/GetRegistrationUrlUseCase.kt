package lol.terabrendon.houseshare2.domain.usecase

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import lol.terabrendon.houseshare2.data.remote.api.NetResult
import lol.terabrendon.houseshare2.data.repository.SessionManager
import timber.log.Timber
import javax.inject.Inject

/**
 * The url of the page where a new user signs up, to be opened in a browser. Once the account is
 * created the user logs in as usual, with [lol.terabrendon.houseshare2.domain.auth.AuthManager.login].
 */
class GetRegistrationUrlUseCase @Inject constructor(
    private val sessionManager: SessionManager,
) {
    suspend operator fun invoke(): NetResult<String> = withContext(Dispatchers.IO) {
        Timber.i("invoke: building the registration url")

        sessionManager.registrationUrl()
    }
}
