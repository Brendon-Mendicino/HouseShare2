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

    companion object {
        /**
         * Asks Spring for the whole collection in a single page. The server still clamps it to
         * its `max-page-size`.
         *
         * Temporary: it stands in for paging until items are synced through events.
         */
        const val UNLIMITED = Int.MAX_VALUE
    }
}
