package lol.terabrendon.houseshare2.data.remote.api

import lol.terabrendon.houseshare2.data.remote.dto.GroupDto
import lol.terabrendon.houseshare2.data.remote.dto.UserDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface UserApi {

    @GET("users")
    suspend fun getUsers(): Page<UserDto>

    @GET("users/{userId}")
    suspend fun getUser(@Path("userId") userId: Long): UserDto

    @POST("users")
    suspend fun save(@Body user: UserDto): UserDto

    @GET("users/{userId}/groups")
    suspend fun getGroups(@Path("userId") userId: Long): List<GroupDto>
}