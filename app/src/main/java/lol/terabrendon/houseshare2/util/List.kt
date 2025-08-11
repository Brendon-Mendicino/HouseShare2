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