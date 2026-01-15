package lol.terabrendon.houseshare2.data.local.util

import android.database.sqlite.SQLiteConstraintException
import android.database.sqlite.SQLiteException
import android.database.sqlite.SQLiteOutOfMemoryException
import androidx.room.RoomDatabase
import androidx.room.withTransaction
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import kotlinx.coroutines.CancellationException
import lol.terabrendon.houseshare2.domain.error.LocalError
import timber.log.Timber

fun mapLocalException(e: Throwable): LocalError = when (e) {
    is SQLiteConstraintException -> LocalError.Constraint(e)
    is SQLiteOutOfMemoryException -> LocalError.OutOfMemory(e)
    is SQLiteException -> LocalError.Unknown(e)
    else -> LocalError.Unknown(e)
}

/**
 * Run safely local-io functions. Wraps the exceptions inside a
 * [LocalResult] type.
 */
suspend inline fun <T> localSafe(
    block: suspend () -> T,
): LocalResult<T> {
    return try {
        Ok(block())
    } catch (e: Throwable) {
        if (e is CancellationException) throw e

        Timber.e(e, "localSafe: an error happened while accessing the database or the file system")
        Err(mapLocalException(e))
    }
}

/**
 * Run safely local-io transactional functions. Wraps the exceptions inside a
 * [LocalResult] type.
 */
suspend fun <T> transactionSafe(
    db: RoomDatabase,
    block: suspend () -> T,
): LocalResult<T> {
    return try {
        Ok(db.withTransaction { block() })
    } catch (e: Throwable) {
        if (e is CancellationException) throw e

        Timber.e(
            e,
            "transactionSafe: an error happened while accessing the database or the file system"
        )
        Err(mapLocalException(e))
    }
}