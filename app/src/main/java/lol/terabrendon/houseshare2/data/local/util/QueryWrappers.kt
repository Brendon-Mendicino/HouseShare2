package lol.terabrendon.houseshare2.data.local.util

import android.database.sqlite.SQLiteConstraintException
import android.database.sqlite.SQLiteException
import android.database.sqlite.SQLiteOutOfMemoryException
import androidx.room.RoomDatabase
import androidx.room.withTransaction
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.CoroutineBindingScope
import com.github.michaelbull.result.coroutines.coroutineBinding
import com.github.michaelbull.result.getOrElse
import kotlinx.coroutines.CancellationException
import lol.terabrendon.houseshare2.domain.error.LocalError
import timber.log.Timber
import javax.annotation.CheckReturnValue

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
@CheckReturnValue
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
@CheckReturnValue
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

class CatchErr(val err: Any?) : Throwable()

/**
 * Like [transactionSafe] but it rolls-back the transaction even in case an [Err] is returned from
 * the [block] function
 */
suspend inline fun <V, reified E> transactionErrSafe(
    db: RoomDatabase,
    crossinline block: suspend () -> Result<V, E>,
): Result<V, E> {
    return try {
        try {
            Ok(db.withTransaction { block().getOrElse { throw CatchErr(it) } })
        } catch (e: CatchErr) {
            Err(e.err as E)
        }
    } catch (e: Throwable) {
        if (e is CancellationException) throw e

        Timber.e(
            e,
            "transactionSafe: an error happened while accessing the database or the file system"
        )
        Err(mapLocalException(e) as E)
    }
}

/**
 * Like [transactionErrSafe] but you calling the [CoroutineBindingScope.bind] inside the [block]
 * function.
 */
suspend inline fun <V, reified E> transactionBinding(
    db: RoomDatabase,
    crossinline block: suspend CoroutineBindingScope<E>.() -> V,
): Result<V, E> {
    return transactionErrSafe(db) {
        coroutineBinding(block)
    }
}