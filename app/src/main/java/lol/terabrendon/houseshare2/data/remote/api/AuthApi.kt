package lol.terabrendon.houseshare2.data.remote.api

import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface AuthApi {
    @GET("oauth2/authorization/house-share-app")
    suspend fun login(): NetResult<Unit>

    @POST("logout")
    suspend fun logout(): NetResult<Unit>

    /**
     * - state
     * - session_state
     * - iss
     * - code
     */
    @GET("login/oauth2/code/house-share-app")
    suspend fun authCodeFlow(
        @Query("state") state: String,
        @Query("session_state") sessionState: String,
        @Query("iss") iss: String,
        @Query("code") code: String,
    ): NetResult<Unit>
}