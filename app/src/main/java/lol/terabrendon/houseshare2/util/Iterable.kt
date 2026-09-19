package lol.terabrendon.houseshare2.util

/**
 * Zip the two iterables together and transform.
 */
fun <T1, T2, O> zip(a: Iterable<T1>, b: Iterable<T2>, transform: (T1, T2) -> O): List<O> =
    a.zip(b).map { (a, b) -> transform(a, b) }
