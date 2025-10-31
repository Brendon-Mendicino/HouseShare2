package lol.terabrendon.houseshare2.data.remote.api

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface LoginApi {
    @GET("oauth2/authorization/house-share-app")
    suspend fun login(): Response<Unit>

    // - state
    // - session_state
    // - iss
    // - code
    @GET("login/oauth2/code/house-share-client")
    suspend fun authCodeFlow(
        @Query("state") state: String,
        @Query("session_state") sessionState: String,
        @Query("iss") iss: String,
        @Query("code") code: String,
    ): Response<Unit>
}