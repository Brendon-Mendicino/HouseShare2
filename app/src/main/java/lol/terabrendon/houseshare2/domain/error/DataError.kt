package lol.terabrendon.houseshare2.domain.error

sealed interface DataError : RootError {
    fun <U> map(local: (LocalError) -> U, remote: (RemoteError) -> U): U = when (this) {
        is LocalError -> local(this)
        is RemoteError -> remote(this)
    }
}