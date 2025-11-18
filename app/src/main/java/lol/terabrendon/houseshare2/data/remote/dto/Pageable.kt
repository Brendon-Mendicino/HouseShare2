package lol.terabrendon.houseshare2.data.remote.dto

data class Pageable(
    val size: Int? = null,
    val page: Int? = null,
    // TODO: convert string to KProperty<T, ...>
    val sort: List<String>? = null,
) {
    fun toMap(): Map<String, Any> {
        val map = mutableMapOf<String, Any>()

        if (size != null)
            map["size"] = size

        if (page != null)
            map["page"] = page

        if (sort != null)
            map["sort"] = sort.joinToString()

        return map.toMap()
    }
}
