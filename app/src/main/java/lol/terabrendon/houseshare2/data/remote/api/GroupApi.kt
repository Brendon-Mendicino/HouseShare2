package lol.terabrendon.houseshare2.data.remote.api

import lol.terabrendon.houseshare2.data.remote.dto.GroupDto
import retrofit2.http.Body
import retrofit2.http.POST

interface GroupApi {
    @POST("groups")
    suspend fun save(@Body group: GroupDto): GroupDto
}