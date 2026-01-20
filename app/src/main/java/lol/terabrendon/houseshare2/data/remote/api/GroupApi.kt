package lol.terabrendon.houseshare2.data.remote.api

import lol.terabrendon.houseshare2.data.remote.dto.GroupDto
import lol.terabrendon.houseshare2.data.remote.dto.InviteUrlDto
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import javax.annotation.CheckReturnValue

@CheckReturnValue
interface GroupApi {
    @POST("groups")
    suspend fun save(@Body group: GroupDto): NetResult<GroupDto>

    @POST("groups/{groupId}/invite")
    suspend fun inviteUrl(@Path("groupId") groupId: Long): NetResult<InviteUrlDto>

    @POST("groups/{groupId}/invite/join")
    suspend fun joinFromInviteUrl(
        @Path("groupId") groupId: Long,
        @Query("expires") expires: Long,
        @Query("nonce") nonce: String,
        @Query("signature") signature: String,
    ): NetResult<GroupDto>
}