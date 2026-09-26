package lol.terabrendon.houseshare2.data.remote.api

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.QueryMap
import javax.annotation.CheckReturnValue

@CheckReturnValue
interface AuthApi {
    /**
     * Asks the server to start the code flow. The answer is a redirect to the identity provider,
     * carrying the authorization request the server built.
     */
    @GET("oauth2/authorization/house-share-app")
    suspend fun login(): Response<Unit>

    @POST("logout")
    suspend fun logout(): NetResult<Unit>

    /**
     * Hands the answer of the provider back to the server, which exchanges the code and opens the
     * session. [params] is forwarded as it comes, the provider decides what it contains.
     */
    @GET("login/oauth2/code/house-share-app")
    suspend fun authCodeFlow(@QueryMap params: Map<String, String>): NetResult<Unit>
}
