package lol.terabrendon.houseshare2.data.repository

import android.util.Log
import androidx.datastore.core.DataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.retryWhen
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import lol.terabrendon.houseshare2.UserPreferences
import lol.terabrendon.houseshare2.presentation.navigation.MainNavigation
import java.io.IOException
import javax.inject.Inject

class UserDataRepositoryImpl @Inject constructor(
    private val userPreferencesStore: DataStore<UserPreferences>,
) : UserDataRepository {
    companion object {
        private const val TAG = "UserPreferencesRepositoryImpl"
    }

    // TODO: remove this function, it should expose classes from data layer!
    private val userPreferencesFlow: Flow<UserPreferences> = userPreferencesStore.data
        .retryWhen { cause, attempt ->
            // dataStore.data throws an IOException when an error is encountered when reading data
            if (cause !is IOException) throw cause

            Log.e(TAG, "userPreferencesFlow: Error reading user preferences.", cause)
            emit(UserPreferences.getDefaultInstance())
            true
        }

    override val savedBackStack: Flow<List<MainNavigation>> = userPreferencesFlow
        .map {
            try {
                Json.decodeFromString<List<MainNavigation>>(it.backStack)
            } catch (e: SerializationException) {
                Log.w(
                    TAG,
                    "savedBackStack: there was an error while deserializing the saved backStack. Providing default.\n" +
                            "${SerializationException::class.qualifiedName}: ${e.message}",
                )
                listOf(MainNavigation.Loading)
            }
        }
        .map { it.ifEmpty { listOf(MainNavigation.Loading) } }

    override suspend fun updateBackStack(backStack: List<MainNavigation>) {
        userPreferencesStore.updateData { preferences ->
            Log.i(TAG, "updateMainDestination: saving backStack to DataStore. $backStack")

            preferences.toBuilder()
                .setBackStack(Json.encodeToString(backStack))
                .build()
        }
    }

    override val currentLoggedUserId: Flow<Long?>
        get() = userPreferencesFlow.map { if (it.currentLoggedUserId == 0L) null else it.currentLoggedUserId }

    override val selectedGroupId: Flow<Long?>
        get() = userPreferencesFlow.map { if (it.selectedGroupId == 0L) null else it.selectedGroupId }

    override suspend fun updateCurrentLoggedUser(userId: Long?) {
        userPreferencesStore.updateData { preferences ->
            Log.i(TAG, "updateCurrentLoggedUser: save userId=$userId to DataStore.")
            preferences
                .toBuilder()
                .setCurrentLoggedUserId(userId ?: 0)
                .build()
        }
    }

    override suspend fun updateSelectedGroupId(groupId: Long?) {
        userPreferencesStore.updateData { preferences ->
            Log.i(TAG, "updateCurrentLoggedUser: save groupId=$groupId to DataStore.")
            preferences
                .toBuilder()
                .setSelectedGroupId(groupId ?: 0)
                .build()
        }
    }
}