package lol.terabrendon.houseshare2.data.remote.api

import lol.terabrendon.houseshare2.data.remote.dto.AppGroupDto
import lol.terabrendon.houseshare2.data.remote.dto.AppUserDto
import lol.terabrendon.houseshare2.data.remote.dto.GroupMemberDto
import lol.terabrendon.houseshare2.data.remote.dto.InviteUrlDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query
import javax.annotation.CheckReturnValue

@CheckReturnValue
interface GroupApi {
    @POST("groups")
    suspend fun save(@Body group: AppGroupDto): NetResult<AppGroupDto>

    @PUT("groups/{groupId}")
    suspend fun update(
        @Path("groupId") groupId: Long,
        @Body group: AppGroupDto,
    ): NetResult<AppGroupDto>

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

    @GET("groups/{groupId}/members")
    suspend fun getGroupMembers(@Path("groupId") groupId: Long): NetResult<List<GroupMemberDto>>

    @GET("groups/{groupId}/members/{memberId}")
    suspend fun getGroupMember(
        @Path("groupId") groupId: Long,
        @Path("memberId") memberId: Long,
    ): NetResult<GroupMemberDto>

    @POST("groups/{groupId}/invite")
    suspend fun inviteUrl(@Path("groupId") groupId: Long): NetResult<InviteUrlDto>

    @POST("groups/{groupId}/invite/join")
    suspend fun joinFromInviteUrl(
        @Path("groupId") groupId: Long,
        @Query("expires") expires: Long,
        @Query("nonce") nonce: String,
        @Query("signature") signature: String,
    ): NetResult<AppGroupDto>
}