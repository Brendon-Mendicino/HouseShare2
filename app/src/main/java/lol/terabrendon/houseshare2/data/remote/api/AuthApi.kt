package lol.terabrendon.houseshare2.data.remote.api

import com.github.michaelbull.result.Result
import lol.terabrendon.houseshare2.domain.error.RemoteError
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface AuthApi {
    @GET("oauth2/authorization/house-share-app")
    suspend fun login(): Result<Unit, RemoteError>

    @POST("logout")
    suspend fun logout(): Response<Unit>

    /**
     * - state
     * - session_state
     * - iss
     * - code
     */
    @GET("login/oauth2/code/house-share-client")
    suspend fun authCodeFlow(
        @Query("state") state: String,
        @Query("session_state") sessionState: String,
        @Query("iss") iss: String,
        @Query("code") code: String,
    ): Response<Unit>
}