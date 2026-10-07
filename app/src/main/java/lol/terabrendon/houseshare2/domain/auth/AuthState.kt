package lol.terabrendon.houseshare2.domain.auth

/**
 * Whether someone is logged in, as far as the app knows. See [AuthManager].
 */
sealed interface AuthState {
    /**
     * The stored session has not been read yet.
     */
    data object Loading : AuthState

    data object LoggedOut : AuthState

    data class LoggedIn(val userId: Long) : AuthState
}
