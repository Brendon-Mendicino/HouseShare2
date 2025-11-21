package lol.terabrendon.houseshare2.data.local.util

import com.github.michaelbull.result.Result
import lol.terabrendon.houseshare2.domain.error.LocalError

typealias LocalResult<T> = Result<T, LocalError>