package lol.terabrendon.houseshare2.data.remote.api

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.FieldMap
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Url

/**
 * Plain access to the identity provider, whose urls are not known at compile time: they are built
 * by the server and by the provider pages themselves.
 *
 * The raw [Response] is returned because the interesting part of these calls is the `Location`
 * header of a redirect, or the body of the login page.
 */
interface IdpApi {
    @GET
    suspend fun get(@Url url: String): Response<ResponseBody>

    @FormUrlEncoded
    @POST
    suspend fun post(
        @Url url: String,
        @FieldMap fields: Map<String, String>,
    ): Response<ResponseBody>
}
