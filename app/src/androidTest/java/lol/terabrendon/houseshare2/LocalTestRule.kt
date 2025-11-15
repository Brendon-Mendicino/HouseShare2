package lol.terabrendon.houseshare2

import android.app.LocaleManager
import android.os.Build
import android.os.LocaleList
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.rules.ExternalResource
import java.util.Locale

class LocaleTestRule(
    private val locale: Locale,
) : ExternalResource() {

    override fun before() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.getSystemService(LocaleManager::class.java)
                .applicationLocales = LocaleList(locale)
        } else {
            AppCompatDelegate.setApplicationLocales(
                LocaleListCompat.create(locale)
            )
        }
    }
}