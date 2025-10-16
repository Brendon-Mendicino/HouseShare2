package lol.terabrendon.houseshare2.util

fun assertEq(left: Any?, right: Any?): Unit {
    assert(left == right) { "Assert failed: left=${left} right=${right}" }
}