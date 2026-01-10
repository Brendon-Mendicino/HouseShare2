package lol.terabrendon.houseshare2.util

/**
 * Provide a list of regex matchers to be checked against an [input] string.
 * If no match is present run the [elseBlk].
 */
suspend fun <O> matcher(
    input: String,
    vararg matchers: Pair<String, suspend (MatchResult) -> O>,
    elseBlk: suspend () -> O,
): O {
    for ((regex, action) in matchers) {
        val compiled = Regex(regex)
        val match = compiled.matchEntire(input)

        if (match == null) {
            continue
        }

        return action(match)
    }

    return elseBlk()
}