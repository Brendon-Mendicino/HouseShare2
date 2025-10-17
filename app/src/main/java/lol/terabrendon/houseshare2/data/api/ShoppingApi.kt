package lol.terabrendon.houseshare2.data.api

import lol.terabrendon.houseshare2.data.dto.ShoppingItemDto
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ShoppingApi {
    @POST("groups/{groupId}/shopping-items")
    suspend fun save(@Path("groupId") groupId: Long, @Body dto: ShoppingItemDto): ShoppingItemDto

    @GET("groups/{groupId}/shopping-items")
    suspend fun getByGroupId(@Path("groupId") groupId: Long): Page<ShoppingItemDto>

    @DELETE("groups/{groupId}/shopping-items/{shoppingItemId}")
    suspend fun delete(@Path("groupId") groupId: Long, @Path("shoppingItemId") shoppingItemId: Long)
}