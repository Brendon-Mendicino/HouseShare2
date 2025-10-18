package lol.terabrendon.houseshare2.di

import android.util.Log
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonNull
import com.google.gson.JsonParseException
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import lol.terabrendon.houseshare2.BuildConfig
import lol.terabrendon.houseshare2.data.api.GroupApi
import lol.terabrendon.houseshare2.data.api.ShoppingApi
import lol.terabrendon.houseshare2.data.api.UserApi
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create
import java.lang.reflect.Type
import java.time.OffsetDateTime
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ApiModule {
    @Provides
    @Singleton
    fun provideRetrofit(): Retrofit = Retrofit.Builder()
        .baseUrl(BuildConfig.BASE_URL)
        .addConverterFactory(
            GsonConverterFactory.create(
                GsonBuilder()
                    // TODO: move to separate file
                    .registerTypeAdapter(
                        OffsetDateTime::class.java,
                        object : JsonDeserializer<OffsetDateTime>, JsonSerializer<OffsetDateTime> {
                            override fun deserialize(
                                json: JsonElement?,
                                typeOfT: Type?,
                                context: JsonDeserializationContext?
                            ): OffsetDateTime? {
                                if (json == null || json.isJsonNull) return null
                                return try {
                                    OffsetDateTime.parse(json.asString)
                                } catch (e: Exception) {
                                    throw JsonParseException(
                                        "Invalid OffsetDateTime format: ${json.asString}",
                                        e
                                    )
                                }
                            }

                            override fun serialize(
                                src: OffsetDateTime?,
                                typeOfSrc: Type?,
                                context: JsonSerializationContext?
                            ): JsonElement? {
                                if (src == null) return JsonNull.INSTANCE
                                return JsonPrimitive(src.toString()) // ISO-8601 format
                            }

                        })
                    .create()
            )
        )
        .client(
            OkHttpClient.Builder()
                .addInterceptor {
                    // TODO: remove this, or integrate with messaging errors API
                    val req = it.request()
                    val res = it.proceed(req)

                    println(res.code)
                    if (!res.isSuccessful) {
                        Log.e("Retrofit", "provideRetrofit: ${res.body?.string()}")
                    }

                    res
                }
                .build()
        )
        .build()

    @Provides
    @Singleton
    fun provideUserApi(retrofit: Retrofit): UserApi = retrofit.create<UserApi>()

    @Provides
    @Singleton
    fun provideGroupApi(retrofit: Retrofit): GroupApi = retrofit.create<GroupApi>()

    @Provides
    @Singleton
    fun providesShoppingApi(retrofit: Retrofit): ShoppingApi = retrofit.create<ShoppingApi>()
}