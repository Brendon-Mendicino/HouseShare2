package lol.terabrendon.houseshare2.data.remote.api

import lol.terabrendon.houseshare2.data.remote.dto.GroupDto
import lol.terabrendon.houseshare2.data.remote.dto.UserDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface UserApi {
    /**
     * Use this to get yourself.
     */
    @GET("users/{userId}")
    suspend fun getUser(@Path("userId") userId: Long): UserDto

    @GET("groups/{groupId}/users")
    suspend fun getGroupUsers(@Path("groupId") groupId: Long): List<UserDto>

    /**
     * Use this route to get users in a group.
     *
     * The [getUser] is protected if you don't have special
     * permissions or if you are not that user.
     */
    @GET("groups/{groupId}/users/{userId}")
    suspend fun getGroupUser(@Path("groupId") groupId: Long, @Path("userId") userId: Long): UserDto

    @GET("users/{userId}/groups")
    suspend fun getGroups(@Path("userId") userId: Long): List<GroupDto>

    @GET("users/logged")
    suspend fun getLoggedUser(): Response<UserDto>
}