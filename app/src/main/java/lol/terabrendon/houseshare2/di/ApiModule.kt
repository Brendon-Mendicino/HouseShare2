package lol.terabrendon.houseshare2.di

import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import lol.terabrendon.houseshare2.BuildConfig
import lol.terabrendon.houseshare2.data.api.GroupApi
import lol.terabrendon.houseshare2.data.api.LoginApi
import lol.terabrendon.houseshare2.data.api.ShoppingApi
import lol.terabrendon.houseshare2.data.api.UserApi
import lol.terabrendon.houseshare2.domain.typeadapter.OffsetDateTimeSerde
import okhttp3.JavaNetCookieJar
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create
import java.net.CookieManager
import java.net.CookiePolicy
import java.time.OffsetDateTime
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ApiModule {
    val cookieManager =
        JavaNetCookieJar(CookieManager().apply { setCookiePolicy(CookiePolicy.ACCEPT_ALL) })

    @Provides
    @Singleton
    fun provideRetrofit(): Retrofit = Retrofit.Builder()
        .baseUrl(BuildConfig.BASE_URL + "api/v1/")
        .addConverterFactory(
            GsonConverterFactory.create(
                GsonBuilder()
                    .registerTypeAdapter(
                        OffsetDateTime::class.java,
                        OffsetDateTimeSerde()
                    )
                    .create()
            )
        )
        .client(
            OkHttpClient.Builder()
                .followRedirects(false)
                .cookieJar(cookieManager)
                .addNetworkInterceptor(HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.HEADERS
                })
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

    @Provides
    @Singleton
    fun provideLogin(): LoginApi {
        val retrofit = Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .client(
                OkHttpClient.Builder()
                    .followRedirects(false)
                    .cookieJar(cookieManager)
                    .addNetworkInterceptor(HttpLoggingInterceptor().apply {
                        level = HttpLoggingInterceptor.Level.HEADERS
                    })
                    .build()
            )
            .build()

        return retrofit.create<LoginApi>()
    }
}