package lol.terabrendon.houseshare2.data.repository

import android.util.Log
import androidx.datastore.core.DataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.retryWhen
import lol.terabrendon.houseshare2.data.local.preferences.UserData
import lol.terabrendon.houseshare2.presentation.navigation.MainNavigation
import java.io.IOException
import javax.inject.Inject

class UserDataRepositoryImpl @Inject constructor(
    private val userPreferencesStore: DataStore<UserData>,
) : UserDataRepository {
    companion object {
        private const val TAG = "UserDataRepositoryImpl"
    }

    private val userPreferencesFlow: Flow<UserData> = userPreferencesStore.data
        .retryWhen { cause, attempt ->
            // dataStore.data throws an IOException when an error is encountered when reading data
            if (cause !is IOException) throw cause

            Log.e(TAG, "userPreferencesFlow: Error reading user preferences.", cause)
            emit(UserData())
            true
        }

    override val savedBackStack: Flow<List<MainNavigation>> = userPreferencesFlow
        .map { it.backStack }
        .map { it.ifEmpty { listOf(MainNavigation.Loading) } }

    override suspend fun updateBackStack(backStack: List<MainNavigation>) {
        Log.i(TAG, "updateMainDestination: saving backStack to DataStore. $backStack")
        userPreferencesStore.updateData { data ->
            data.copy(backStack = backStack)
        }
    }

    override val currentLoggedUserId: Flow<Long?>
        get() = userPreferencesFlow
            .map { data -> data.currentLoggedUserId.takeIf { 0L != it } }

    override val selectedGroupId: Flow<Long?>
        get() = userPreferencesFlow
            .map { data -> data.selectedGroupId.takeIf { 0L != it } }
            .distinctUntilChanged()

    override suspend fun updateCurrentLoggedUser(userId: Long?) {
        Log.i(TAG, "updateCurrentLoggedUser: save userId=$userId to DataStore.")
        userPreferencesStore.updateData { data ->
            data.copy(currentLoggedUserId = userId)
        }
    }

    override suspend fun updateSelectedGroupId(groupId: Long?) {
        Log.i(TAG, "updateCurrentLoggedUser: save groupId=$groupId to DataStore.")
        userPreferencesStore.updateData { data ->
            data.copy(selectedGroupId = groupId)
        }
    }
}