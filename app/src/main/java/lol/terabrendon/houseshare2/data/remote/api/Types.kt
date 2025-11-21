package lol.terabrendon.houseshare2.data.remote.api

import com.github.michaelbull.result.Result
import lol.terabrendon.houseshare2.domain.error.RemoteError

typealias NetResult<T> = Result<T, RemoteError>