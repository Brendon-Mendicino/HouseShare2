package lol.terabrendon.houseshare2.di

import android.content.Context
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import lol.terabrendon.houseshare2.BuildConfig
import lol.terabrendon.houseshare2.data.remote.api.AuthApi
import lol.terabrendon.houseshare2.data.remote.api.CsrfInterceptor
import lol.terabrendon.houseshare2.data.remote.api.ExpenseApi
import lol.terabrendon.houseshare2.data.remote.api.GroupApi
import lol.terabrendon.houseshare2.data.remote.api.ResultCallAdapterFactory
import lol.terabrendon.houseshare2.data.remote.api.SharedCookieIndexStore
import lol.terabrendon.houseshare2.data.remote.api.SharedPrefCookieStore
import lol.terabrendon.houseshare2.data.remote.api.ShoppingApi
import lol.terabrendon.houseshare2.data.remote.api.UserApi
import lol.terabrendon.houseshare2.domain.typeadapter.OffsetDateTimeSerde
import okhttp3.CookieJar
import okhttp3.JavaNetCookieJar
import okhttp3.OkHttpClient
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
    @Provides
    @Singleton
    fun provideCookieManager(
        @ApplicationContext
        context: Context,
    ): CookieJar =
        JavaNetCookieJar(
            CookieManager(
                SharedPrefCookieStore(
                    SharedCookieIndexStore(
                        context.getSharedPreferences(
                            "cookie_index",
                            Context.MODE_PRIVATE
                        )
                    )
                ),
                null,
            ).apply { setCookiePolicy(CookiePolicy.ACCEPT_ALL) })


    private val csrfManager = CsrfInterceptor()

    @Provides
    @Singleton
    fun provideRetrofit(cookieManager: CookieJar): Retrofit = Retrofit.Builder()
        .baseUrl(BuildConfig.BASE_URL + "api/v1/")
        .addCallAdapterFactory(ResultCallAdapterFactory.create())
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
                .addNetworkInterceptor(csrfManager)
//                .addNetworkInterceptor(HttpLoggingInterceptor().apply {
//                    level = HttpLoggingInterceptor.Level.HEADERS
//                })
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
    fun provideExpenseApi(retrofit: Retrofit): ExpenseApi = retrofit.create<ExpenseApi>()

    @Provides
    @Singleton
    fun provideLogin(cookieManager: CookieJar): AuthApi {
        val retrofit = Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .addCallAdapterFactory(ResultCallAdapterFactory.create())
            .client(
                OkHttpClient.Builder()
                    .followRedirects(false)
                    .cookieJar(cookieManager)
                    .addNetworkInterceptor(csrfManager)
//                    .addNetworkInterceptor(HttpLoggingInterceptor().apply {
//                        level = HttpLoggingInterceptor.Level.HEADERS
//                    })
                    .build()
            )
            .build()

        return retrofit.create<AuthApi>()
    }
}