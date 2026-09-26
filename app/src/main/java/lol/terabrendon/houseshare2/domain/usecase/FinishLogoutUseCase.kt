package lol.terabrendon.houseshare2.domain.usecase

import androidx.room.RoomDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import lol.terabrendon.houseshare2.data.repository.UserDataRepository
import lol.terabrendon.houseshare2.data.repository.UserDataRepository.Update.BackStack
import lol.terabrendon.houseshare2.data.repository.UserDataRepository.Update.LoggedUserId
import lol.terabrendon.houseshare2.data.repository.UserDataRepository.Update.SelectedGroupId
import lol.terabrendon.houseshare2.presentation.navigation.MainNavigation
import timber.log.Timber
import java.net.CookieStore
import javax.inject.Inject

class FinishLogoutUseCase @Inject constructor(
    private val userDataRepository: UserDataRepository,
    private val cookieStore: CookieStore,
    private val db: RoomDatabase,
) {
    suspend operator fun invoke() = withContext(Dispatchers.IO) {
        db.clearAllTables()

        // Both sessions live here, the one of the server and the one of the identity provider.
        // They used to survive the logout, which made the next login start half authenticated.
        cookieStore.removeAll()

        userDataRepository.update(LoggedUserId(null))
        userDataRepository.update(SelectedGroupId(null))
        userDataRepository.update(BackStack(listOf(MainNavigation.Login)))

        Timber.i("invoke: logout completed! DB cleared of all previous stored data.")
    }
}
