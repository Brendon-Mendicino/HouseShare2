package lol.terabrendon.houseshare2.data.repository

import android.util.Log
import androidx.datastore.core.DataStore
import com.github.michaelbull.result.getOrElse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import lol.terabrendon.houseshare2.UserPreferences
import lol.terabrendon.houseshare2.presentation.navigation.MainNavigation
import lol.terabrendon.houseshare2.presentation.navigation.MainNavigation.Companion.toPreferences
import java.io.IOException
import javax.inject.Inject
import kotlin.reflect.KClass

class UserPreferencesRepositoryImpl @Inject constructor(
    private val userPreferencesStore: DataStore<UserPreferences>,
) : UserPreferencesRepository {
    companion object {
        private const val TAG = "UserPreferencesRepositoryImpl"
    }

    // TODO: remove this function, it should expose classes from data layer!
    private val userPreferencesFlow: Flow<UserPreferences> = userPreferencesStore.data
        .catch { exception ->
            // dataStore.data throws an IOException when an error is encountered when reading data
            if (exception is IOException) {
                Log.e(TAG, "Error reading user preferences.", exception)
                emit(UserPreferences.getDefaultInstance())
            } else {
                throw exception
            }
        }

    override val savedDestination: Flow<MainNavigation> = userPreferencesFlow
        .map {
            when (it.mainDestination) {
                // TODO: double check this routes (especially UNRECOGNIZED)
                UserPreferences.MainDestination.CLEANING -> MainNavigation.Cleaning
                UserPreferences.MainDestination.SHOPPING -> MainNavigation.Shopping
                UserPreferences.MainDestination.BILLING -> MainNavigation.Billing
                UserPreferences.MainDestination.GROUPS -> MainNavigation.Groups(it.currentLoggedUserId)
                UserPreferences.MainDestination.UNSPECIFIED -> MainNavigation.Cleaning
                UserPreferences.MainDestination.UNRECOGNIZED -> MainNavigation.Cleaning
            }
        }

    override val topLevelRoutes: Flow<List<MainNavigation>> = userPreferencesFlow
        .map {
            listOf(
                MainNavigation.Cleaning,
                MainNavigation.Shopping,
                MainNavigation.Billing,
                MainNavigation.Groups(it.currentLoggedUserId),
            )
        }

    override suspend fun updateMainDestination(destination: KClass<out MainNavigation>) {
        userPreferencesStore.updateData { preferences ->
            Log.i(TAG, "updateMainDestination: saving ${destination.qualifiedName} to DataStore.")
            preferences.toBuilder()
                .setMainDestination(destination.toPreferences().getOrElse {
                    val msg =
                        "updateMainDestination: could not convert destination to preference: ${it.message}"
                    Log.e(TAG, msg)
                    throw RuntimeException(msg, it)
                })
                .build()
        }
    }
}