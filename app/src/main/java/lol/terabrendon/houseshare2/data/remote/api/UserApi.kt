package lol.terabrendon.houseshare2.data.remote.api

import lol.terabrendon.houseshare2.data.remote.dto.AppGroupDto
import lol.terabrendon.houseshare2.data.remote.dto.AppUserDto
import retrofit2.http.GET
import retrofit2.http.Path
import javax.annotation.CheckReturnValue

@CheckReturnValue
interface UserApi {
    @GET("groups/{groupId}/users")
    suspend fun getGroupUsers(@Path("groupId") groupId: Long): NetResult<List<AppUserDto>>

    /**
     * Use this route to get users in a group.
     */
    @GET("groups/{groupId}/users/{userId}")
    suspend fun getGroupUser(
        @Path("groupId") groupId: Long,
        @Path("userId") userId: Long,
    ): NetResult<AppUserDto>

    @GET("users/{userId}/groups")
    suspend fun getGroups(@Path("userId") userId: Long): NetResult<List<AppGroupDto>>

    @GET("users/logged")
    suspend fun getLoggedUser(): NetResult<AppUserDto>
}