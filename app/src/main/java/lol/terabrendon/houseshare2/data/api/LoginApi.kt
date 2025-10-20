package lol.terabrendon.houseshare2.data.api

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Url

interface LoginApi {
    @GET("oauth2/authorization/house-share-app")
    suspend fun login(): Response<Unit>

    // TODO: should I change this or not?
    @GET
    suspend fun authCodeFlow(@Url url: String): Response<Unit>
}