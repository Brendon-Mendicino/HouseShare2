package lol.terabrendon.houseshare2.data.remote.idp

/**
 * The login form served by Keycloak.
 *
 * [fields] holds every field the page carries on its own (the hidden ones, plus whatever the
 * action url already encodes), so that the credentials are posted back exactly in the shape the
 * provider expects, without hardcoding anything about it.
 */
data class LoginForm(
    val action: String,
    val fields: Map<String, String>,
    /**
     * Whether the page offers to be remembered. When it does, the session of the provider is kept
     * alive by a persistent cookie instead of a short lived one, which is what lets the app renew
     * a session days later without asking the password again.
     */
    val rememberMe: Boolean,
)

/**
 * Reads what is needed out of a Keycloak login page. Everything specific to the markup of the
 * provider is confined here, see [KeycloakAuthenticator].
 */
object KeycloakLoginPage {
    private const val FORM_ID = "kc-form-login"

    private val FORM_TAG = Regex("""<form\b[^>]*>""", RegexOption.IGNORE_CASE)
    private val ACTION = Regex("""\baction\s*=\s*"([^"]*)"""", RegexOption.IGNORE_CASE)
    private val HIDDEN_INPUT = Regex(
        """<input\b[^>]*type\s*=\s*"hidden"[^>]*>""",
        RegexOption.IGNORE_CASE,
    )
    private val NAME = Regex("""\bname\s*=\s*"([^"]*)"""", RegexOption.IGNORE_CASE)
    private val VALUE = Regex("""\bvalue\s*=\s*"([^"]*)"""", RegexOption.IGNORE_CASE)
    private val REMEMBER_ME = Regex(
        """<input\b[^>]*name\s*=\s*"rememberMe"[^>]*>""",
        RegexOption.IGNORE_CASE,
    )
    private val ERROR = Regex(
        """<span\b[^>]*(?:id\s*=\s*"input-error"|class\s*=\s*"[^"]*kc-feedback-text[^"]*")[^>]*>(.*?)</span>""",
        setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL),
    )

    /**
     * The login form of [html], or `null` when the page does not hold one, which means the page
     * is not the one this code knows how to fill.
     */
    fun parse(html: String): LoginForm? {
        val tag = FORM_TAG.findAll(html).firstOrNull { it.value.contains(FORM_ID) } ?: return null
        val action = ACTION.find(tag.value)?.groupValues?.get(1)?.unescape() ?: return null

        val fields = HIDDEN_INPUT
            .findAll(html)
            .mapNotNull { input ->
                val name = NAME.find(input.value)?.groupValues?.get(1) ?: return@mapNotNull null
                val value = VALUE.find(input.value)?.groupValues?.get(1)?.unescape() ?: ""

                name to value
            }
            .toMap()

        return LoginForm(
            action = action,
            fields = fields,
            rememberMe = REMEMBER_ME.containsMatchIn(html),
        )
    }

    /**
     * The message the provider shows on the page, e.g. why the credentials were refused.
     */
    fun errorMessage(html: String): String? = ERROR
        .find(html)
        ?.groupValues
        ?.get(1)
        ?.replace(Regex("<[^>]*>"), " ")
        ?.unescape()
        ?.trim()
        ?.takeIf { it.isNotEmpty() }

    private val ENTITIES = mapOf(
        "&amp;" to "&",
        "&lt;" to "<",
        "&gt;" to ">",
        "&quot;" to "\"",
        "&#39;" to "'",
        "&#x27;" to "'",
        "&nbsp;" to " ",
    )

    private fun String.unescape(): String =
        ENTITIES.entries.fold(this) { acc, (entity, char) -> acc.replace(entity, char) }
}
