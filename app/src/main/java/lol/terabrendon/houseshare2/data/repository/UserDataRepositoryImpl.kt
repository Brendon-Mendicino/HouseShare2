package lol.terabrendon.houseshare2.data.repository

import androidx.datastore.core.DataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.retryWhen
import lol.terabrendon.houseshare2.data.local.preferences.UserData
import lol.terabrendon.houseshare2.data.repository.UserDataRepository.Update
import lol.terabrendon.houseshare2.presentation.navigation.MainNavigation
import timber.log.Timber
import java.io.IOException
import javax.inject.Inject

class UserDataRepositoryImpl @Inject constructor(
    private val userPreferencesStore: DataStore<UserData>,
) : UserDataRepository {
    private val userPreferencesFlow: Flow<UserData> = userPreferencesStore.data
        .retryWhen { cause, attempt ->
            // dataStore.data throws an IOException when an error is encountered when reading data
            if (cause !is IOException) throw cause

            Timber.e(cause, "userPreferencesFlow: Error reading user preferences.")
            emit(UserData())
            true
        }

    override val savedBackStack: Flow<List<MainNavigation>> = userPreferencesFlow
        .map { it.backStack }
        .map { it.ifEmpty { listOf(MainNavigation.Loading) } }

    override val currentLoggedUserId: Flow<Long?>
        get() = userPreferencesFlow
            .map { data -> data.currentLoggedUserId.takeIf { 0L != it } }

    override val selectedGroupId: Flow<Long?>
        get() = userPreferencesFlow
            .map { data -> data.selectedGroupId.takeIf { 0L != it } }
            .distinctUntilChanged()

    override val termsAndConditions: Flow<Boolean>
        get() = userPreferencesFlow
            .map { data -> data.termsAndConditions }
            .distinctUntilChanged()

    override val sendAnalytics: Flow<Boolean>
        get() = userPreferencesFlow
            .map { data -> data.sendAnalytics }
            .distinctUntilChanged()

    override suspend fun update(update: Update) {
        Timber.i("updating UserData: update=%s", update)
        userPreferencesStore.updateData { data ->
            when (update) {
                is Update.BackStack -> data.copy(backStack = update.backStack)
                is Update.SelectedGroupId -> data.copy(selectedGroupId = update.groupId)
                is Update.LoggedUserId -> data.copy(currentLoggedUserId = update.userId)
                is Update.SendAnalytics -> data.copy(sendAnalytics = update.accept)
                is Update.TermsConditions -> data.copy(termsAndConditions = update.accept)
            }
        }
    }
}