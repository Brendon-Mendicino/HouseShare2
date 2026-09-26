package lol.terabrendon.houseshare2.data.local.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.dataStore

private const val COOKIE_DATA_FILE_NAME = "cookie_data.bin"

val Context.cookiePreferencesStore: DataStore<CookieData> by dataStore(
    fileName = COOKIE_DATA_FILE_NAME,
    serializer = CookieDataSerializer(),
    // Cookies that cannot be read are forgotten, the user logs in again.
    corruptionHandler = ReplaceFileCorruptionHandler { CookieData() },
)
