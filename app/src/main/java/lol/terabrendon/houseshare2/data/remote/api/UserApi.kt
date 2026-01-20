package lol.terabrendon.houseshare2.data.remote.api

import lol.terabrendon.houseshare2.data.remote.dto.GroupDto
import lol.terabrendon.houseshare2.data.remote.dto.UserDto
import retrofit2.http.GET
import retrofit2.http.Path
import javax.annotation.CheckReturnValue

@CheckReturnValue
interface UserApi {
    @GET("groups/{groupId}/users")
    suspend fun getGroupUsers(@Path("groupId") groupId: Long): NetResult<List<UserDto>>

    /**
     * Use this route to get users in a group.
     */
    @GET("groups/{groupId}/users/{userId}")
    suspend fun getGroupUser(
        @Path("groupId") groupId: Long,
        @Path("userId") userId: Long,
    ): NetResult<UserDto>

    @GET("users/{userId}/groups")
    suspend fun getGroups(@Path("userId") userId: Long): NetResult<List<GroupDto>>

    @GET("users/logged")
    suspend fun getLoggedUser(): NetResult<UserDto>
}