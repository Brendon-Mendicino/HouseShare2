package lol.terabrendon.houseshare2.domain.mapper

interface Mapper<IN, OUT> {
    fun map(it: IN): OUT
}