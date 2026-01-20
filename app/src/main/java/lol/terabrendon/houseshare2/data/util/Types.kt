package lol.terabrendon.houseshare2.data.util

import com.github.michaelbull.result.Result
import lol.terabrendon.houseshare2.domain.error.DataError

typealias DataResult<T> = Result<T, DataError>