package lol.terabrendon.houseshare2.data.repository

import android.util.Log
import androidx.datastore.core.DataStore
import com.github.michaelbull.result.getOrElse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import lol.terabrendon.houseshare2.UserPreferences
import lol.terabrendon.houseshare2.presentation.navigation.HomepageNavigation
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
                UserPreferences.MainDestination.CLEANING -> HomepageNavigation.Cleaning
                UserPreferences.MainDestination.SHOPPING -> HomepageNavigation.Shopping
                UserPreferences.MainDestination.BILLING -> HomepageNavigation.Billing
                UserPreferences.MainDestination.GROUPS -> HomepageNavigation.Groups
                UserPreferences.MainDestination.LOGIN -> MainNavigation.Login
                UserPreferences.MainDestination.UNSPECIFIED -> MainNavigation.Login
                UserPreferences.MainDestination.UNRECOGNIZED -> MainNavigation.Login
            }
        }

    // TODO: rename to homepageRoutes
    override val topLevelRoutes: Flow<List<MainNavigation>> = userPreferencesFlow
        .map {
            listOf(
                HomepageNavigation.Cleaning,
                HomepageNavigation.Shopping,
                HomepageNavigation.Billing,
                HomepageNavigation.Groups,
            )
        }

    override val currentLoggedUserId: Flow<Long?>
        get() = userPreferencesFlow.map { if (it.currentLoggedUserId == 0L) null else it.currentLoggedUserId }

    override val selectedGroupId: Flow<Long?>
        get() = userPreferencesFlow.map { if (it.selectedGroupId == 0L) null else it.selectedGroupId }

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

    override suspend fun updateCurrentLoggedUser(userId: Long) {
        userPreferencesStore.updateData { preferences ->
            Log.i(TAG, "updateCurrentLoggedUser: save userId=$userId to DataStore.")
            preferences
                .toBuilder()
                .setCurrentLoggedUserId(userId)
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