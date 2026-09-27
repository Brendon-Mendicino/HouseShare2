package lol.terabrendon.houseshare2.data.local.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.dataStore
import lol.terabrendon.houseshare2.BuildConfig
import lol.terabrendon.houseshare2.data.local.crypto.KeystoreCrypto
import lol.terabrendon.houseshare2.data.local.crypto.PlainCrypto

/**
 * The debug build keeps the cookies in plain json, readable with
 * `adb shell run-as lol.terabrendon.houseshare2 cat files/datastore/cookie_data.json`.
 *
 * Both files are excluded from the backups, keep them in sync with `res/xml/backup_rules.xml`
 * and `res/xml/data_extraction_rules.xml`.
 */
private val COOKIE_DATA_FILE_NAME = if (BuildConfig.DEBUG) "cookie_data.json" else "cookie_data.bin"

val Context.cookiePreferencesStore: DataStore<CookieData> by dataStore(
    fileName = COOKIE_DATA_FILE_NAME,
    serializer = CookieDataSerializer(if (BuildConfig.DEBUG) PlainCrypto else KeystoreCrypto),
    // Cookies that cannot be read are forgotten, the user logs in again.
    corruptionHandler = ReplaceFileCorruptionHandler { CookieData() },
)
