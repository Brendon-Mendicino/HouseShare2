package lol.terabrendon.houseshare2.data.remote.api

import lol.terabrendon.houseshare2.data.remote.dto.ExpenseDto
import lol.terabrendon.houseshare2.data.remote.dto.Page
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ExpenseApi {
    @GET("groups/{groupId}/expenses")
    suspend fun getExpenses(@Path("groupId") groupId: Long): Page<ExpenseDto>

    @POST("groups/{groupId}/expenses")
    suspend fun save(@Path("groupId") groupId: Long, @Body expense: ExpenseDto): ExpenseDto
}