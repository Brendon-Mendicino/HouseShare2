package lol.terabrendon.houseshare2.domain.usecase

import androidx.room.RoomDatabase
import androidx.room.withTransaction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import lol.terabrendon.houseshare2.data.repository.UserDataRepository
import lol.terabrendon.houseshare2.data.repository.UserDataRepository.Update.BackStack
import lol.terabrendon.houseshare2.data.repository.UserDataRepository.Update.LoggedUserId
import lol.terabrendon.houseshare2.data.repository.UserDataRepository.Update.SelectedGroupId
import lol.terabrendon.houseshare2.presentation.navigation.MainNavigation
import timber.log.Timber
import javax.inject.Inject

class FinishLogoutUseCase @Inject constructor(
    private val userDataRepository: UserDataRepository,
    private val db: RoomDatabase,
) {
    suspend operator fun invoke() = withContext(Dispatchers.IO) {
        db.clearAllTables()

        db.withTransaction {
            userDataRepository.update(LoggedUserId(null))
            userDataRepository.update(SelectedGroupId(null))
            userDataRepository.update(BackStack(listOf(MainNavigation.Login)))
        }

        Timber.i("invoke: logout completed! DB cleared of all previous stored data.")
    }
}