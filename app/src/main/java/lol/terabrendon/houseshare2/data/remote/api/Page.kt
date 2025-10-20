package lol.terabrendon.houseshare2.data.remote.api

data class Page<T>(
    val content: List<T>,
    val page: PageInfo,
) {
    data class PageInfo(
        val size: Int,
        val number: Int,
        val totalElements: Int,
        val totalPages: Int,
    )
}
