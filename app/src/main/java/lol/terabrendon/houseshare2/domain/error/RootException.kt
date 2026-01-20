package lol.terabrendon.houseshare2.domain.error

/**
 * Used to throw exceptions in the externalScope blocks. This is useful because
 * when launching an `externalScope.launch` coroutine, it's handy to know
 * where the error comes from if we are handling errors with the [com.github.michaelbull.result.Result]
 * types.
 *
 * Following this practice of throwing [RootException] inside external scopes, we achieve
 * a logging in the [lol.terabrendon.houseshare2.HouseShareApplication] of results as well.
 */
class RootException(
    val err: RootError,
    override val message: String? = null,
    override val cause: Throwable? = null,
) : Exception(message, cause)

//fun <V, E : RootError> Result<V, E>.getOrThrow() = getOrThrow { RootException(it) }