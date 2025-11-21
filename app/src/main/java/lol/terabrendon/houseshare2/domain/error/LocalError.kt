package lol.terabrendon.houseshare2.domain.error

sealed interface LocalError : DataError {
    val t: Throwable?

    data class Constraint(override val t: Throwable? = null) : LocalError
    data class OutOfMemory(override val t: Throwable? = null) : LocalError
    data class Unknown(override val t: Throwable? = null) : LocalError
}