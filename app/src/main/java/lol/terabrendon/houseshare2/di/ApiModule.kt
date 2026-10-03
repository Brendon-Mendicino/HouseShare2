package lol.terabrendon.houseshare2.di

import android.content.Context
import com.google.gson.GsonBuilder
import dagger.Lazy
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import lol.terabrendon.houseshare2.BuildConfig
import lol.terabrendon.houseshare2.data.local.preferences.cookiePreferencesStore
import lol.terabrendon.houseshare2.data.remote.api.AuthApi
import lol.terabrendon.houseshare2.data.remote.api.ExpenseApi
import lol.terabrendon.houseshare2.data.remote.api.GroupApi
import lol.terabrendon.houseshare2.data.remote.api.IdpApi
import lol.terabrendon.houseshare2.data.remote.api.ResultCallAdapterFactory
import lol.terabrendon.houseshare2.data.remote.api.SharedPrefCookieStore
import lol.terabrendon.houseshare2.data.remote.api.ShoppingApi
import lol.terabrendon.houseshare2.data.remote.api.UserApi
import lol.terabrendon.houseshare2.data.remote.idp.CsrfInterceptor
import lol.terabrendon.houseshare2.data.remote.interceptor.BaseUrlInterceptor
import lol.terabrendon.houseshare2.data.remote.interceptor.HttpLoggingInterceptor
import lol.terabrendon.houseshare2.data.remote.interceptor.HttpLoggingInterceptor.Level
import lol.terabrendon.houseshare2.data.remote.interceptor.SessionRenewInterceptor
import lol.terabrendon.houseshare2.data.repository.SessionManager
import lol.terabrendon.houseshare2.domain.typeadapter.OffsetDateTimeSerde
import lol.terabrendon.houseshare2.util.applyIf
import okhttp3.CookieJar
import okhttp3.JavaNetCookieJar
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create
import java.net.CookieManager
import java.net.CookiePolicy
import java.net.CookieStore
import java.time.OffsetDateTime
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ApiModule {
    private val csrfManager = CsrfInterceptor()

    private val baseUrlInterceptorEnabled = BuildConfig.DEBUG

    // Debug builds can point the app to another server, see DebugServerUrl.
    private val baseUrlInterceptor = BaseUrlInterceptor()

    // Logs full request/response bodies in debug builds only, so cookies/PII never
    // hit Logcat in release.
    private val loggingInterceptor = HttpLoggingInterceptor(
        level = if (BuildConfig.DEBUG) Level.BODY else Level.NONE,
    )

    @Provides
    @Singleton
    fun provideCookieStore(
        @ApplicationContext
        context: Context,
    ): CookieStore = SharedPrefCookieStore(context.cookiePreferencesStore)

    @Provides
    @Singleton
    fun provideCookieManager(cookieStore: CookieStore): CookieJar =
        JavaNetCookieJar(
            CookieManager(cookieStore, null)
                .apply { setCookiePolicy(CookiePolicy.ACCEPT_ALL) })


    @Provides
    @Singleton
    fun provideRetrofit(
        cookieManager: CookieJar,
        sessionManager: Lazy<SessionManager>,
    ): Retrofit = Retrofit.Builder()
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
                .applyIf(baseUrlInterceptorEnabled) {
                    addInterceptor(baseUrlInterceptor)
                }
                .addNetworkInterceptor(csrfManager)
                // Before the logging one, so that the replayed request is logged as well.
                .addInterceptor(SessionRenewInterceptor(sessionManager))
                .addInterceptor(loggingInterceptor)
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
            .addConverterFactory(GsonConverterFactory.create())
            .client(
                OkHttpClient.Builder()
                    .followRedirects(false)
                    .cookieJar(cookieManager)
                    .applyIf(baseUrlInterceptorEnabled) {
                        addInterceptor(baseUrlInterceptor)
                    }
                    .addNetworkInterceptor(csrfManager)
                    .addInterceptor(loggingInterceptor)
                    .build()
            )
            .build()

        return retrofit.create<AuthApi>()
    }

    /**
     * The identity provider is reached with the same cookie jar as the server: its session
     * cookies are persisted exactly like the server ones, which is what allows a login to be
     * renewed without asking the credentials again.
     *
     * Every url of this client is absolute and comes from the server or from the provider pages,
     * the base url is only there because Retrofit requires one.
     */
    @Provides
    @Singleton
    @IdpRetrofit
    fun provideIdpRetrofit(cookieManager: CookieJar): Retrofit = Retrofit.Builder()
        .baseUrl(BuildConfig.BASE_URL)
        .client(
            OkHttpClient.Builder()
                .followRedirects(false)
                .cookieJar(cookieManager)
                .applyIf(baseUrlInterceptorEnabled) {
                    addInterceptor(baseUrlInterceptor)
                }
                .addInterceptor(loggingInterceptor)
                .build()
        )
        .build()

    @Provides
    @Singleton
    fun provideIdpApi(@IdpRetrofit retrofit: Retrofit): IdpApi = retrofit.create<IdpApi>()
}
