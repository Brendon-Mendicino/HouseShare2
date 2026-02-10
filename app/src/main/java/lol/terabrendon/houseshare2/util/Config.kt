package lol.terabrendon.houseshare2.util

import lol.terabrendon.houseshare2.BuildConfig

object Config {
    const val TERMS_URL = BuildConfig.BASE_URL + "public/terms-conditions.html"
    const val PRIVACY_URL = BuildConfig.BASE_URL + "public/privacy.html"
    const val ACCOUNT_URL = BuildConfig.BASE_URL + "public/account"

    val LANG_LOCALES = mapOf(
        "English" to "en",
        "Italiano" to "it",
    )
}