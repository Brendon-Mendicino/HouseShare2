package lol.terabrendon.houseshare2.data.remote.api

import lol.terabrendon.houseshare2.data.remote.dto.AppGroupDto
import lol.terabrendon.houseshare2.data.remote.dto.AppUserDto
import retrofit2.http.GET
import retrofit2.http.Path
import javax.annotation.CheckReturnValue

@CheckReturnValue
interface UserApi {
    @GET("users/{userId}/groups")
    suspend fun getGroups(@Path("userId") userId: Long): NetResult<List<AppGroupDto>>

    @GET("users/logged")
    suspend fun getLoggedUser(): NetResult<AppUserDto>
}