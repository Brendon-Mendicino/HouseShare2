@file:Suppress("unused")

package lol.terabrendon.houseshare2.util

fun <T> List<T>.splitAt(mid: Int): Pair<List<T>, List<T>> {
    val first = ArrayList<T>()
    val second = ArrayList<T>()

    forEachIndexed { i, e ->
        if (i < mid)
            first.add(e)
        else
            second.add(e)
    }

    return Pair(first, second)
}

/**
 * Get an item with reverse indexes. This is like
 * indexing an array in python with -1.
 */
fun <T> List<T>.end(index: Int) = get(size + index)

fun <T> List<T>.update(index: Int, transform: (T) -> T): List<T> {
    val mutable = toMutableList()
    mutable[index] = transform(mutable[index])
    return mutable.toList()
}