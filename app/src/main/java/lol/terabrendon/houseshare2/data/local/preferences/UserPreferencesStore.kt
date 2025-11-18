package lol.terabrendon.houseshare2.data.local.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.dataStore

private const val USER_DATA_FILE_NAME = "user_data.json"

val Context.userPreferencesStore: DataStore<UserData> by dataStore(
    fileName = USER_DATA_FILE_NAME,
    serializer = UserDataSerializer(),
)
