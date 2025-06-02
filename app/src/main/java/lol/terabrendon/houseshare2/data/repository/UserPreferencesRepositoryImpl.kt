package lol.terabrendon.houseshare2.data.repository

import android.util.Log
import androidx.datastore.core.DataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import lol.terabrendon.houseshare2.UserPreferences
import lol.terabrendon.houseshare2.presentation.navigation.MainDestination
import java.io.IOException
import javax.inject.Inject

class UserPreferencesRepositoryImpl @Inject constructor(
    private val userPreferencesStore: DataStore<UserPreferences>
) : UserPreferencesRepository {
    companion object {
        private const val TAG = "UserPreferencesRepositoryImpl"
    }

    override val userPreferencesFlow: Flow<UserPreferences> = userPreferencesStore.data
        .catch { exception ->
            // dataStore.data throws an IOException when an error is encountered when reading data
            if (exception is IOException) {
                Log.e(TAG, "Error reading user preferences.", exception)
                emit(UserPreferences.getDefaultInstance())
            } else {
                throw exception
            }
        }

    override suspend fun updateMainDestination(destination: MainDestination) {
        userPreferencesStore.updateData { preferences ->
            Log.i(TAG, "updateMainDestination: saving $destination to DataStore.")
            preferences.toBuilder().setMainDestination(destination.toPreferences()).build()
        }
    }
}