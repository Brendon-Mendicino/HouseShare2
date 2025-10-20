package lol.terabrendon.houseshare2.data.remote.api

import lol.terabrendon.houseshare2.data.remote.dto.CheckDto
import lol.terabrendon.houseshare2.data.remote.dto.ShoppingItemDto
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

    @POST("shopping-items/{shoppingItemId}/checkoff")
    suspend fun checkShoppingItem(
        @Path("shoppingItemId") shoppingItemId: Long,
        @Body dto: CheckDto
    ): CheckDto

    @DELETE("shopping-items/{shoppingItemId}/checkoff")
    suspend fun uncheckShoppingItem(
        @Path("shoppingItemId") shoppingItemId: Long
    )
}