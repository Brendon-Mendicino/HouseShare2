package lol.terabrendon.houseshare2.di

import android.app.Application
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import lol.terabrendon.houseshare2.BuildConfig
import lol.terabrendon.houseshare2.HouseShareApplication
import lol.terabrendon.houseshare2.UserPreferences
import lol.terabrendon.houseshare2.data.api.GroupApi
import lol.terabrendon.houseshare2.data.api.UserApi
import lol.terabrendon.houseshare2.data.dao.CheckoffStateDao
import lol.terabrendon.houseshare2.data.dao.ExpenseDao
import lol.terabrendon.houseshare2.data.dao.GroupDao
import lol.terabrendon.houseshare2.data.dao.ShoppingItemDao
import lol.terabrendon.houseshare2.data.dao.UserDao
import lol.terabrendon.houseshare2.data.database.HouseShareDatabase
import lol.terabrendon.houseshare2.data.preferences.userPreferencesStore
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideExternalScope(application: Application): CoroutineScope =
        (application as HouseShareApplication).applicationScope

    @IoDispatcher
    @Provides
    fun provideIoDispatcher(): CoroutineDispatcher = Dispatchers.IO

    @Provides
    @Singleton
    fun provideUserPreferencesStore(@ApplicationContext applicationContext: Context): DataStore<UserPreferences> =
        applicationContext.userPreferencesStore

    @Provides
    @Singleton
    fun provideHouseShareDatabase(@ApplicationContext applicationContext: Context): HouseShareDatabase {
        return Room.databaseBuilder(
            applicationContext, HouseShareDatabase::class.java, "house_share_db"
        ).build()
    }

    @Provides
    @Singleton
    fun provideShoppingItemDao(db: HouseShareDatabase): ShoppingItemDao = db.shoppingItemDao()

    @Provides
    @Singleton
    fun provideExpenseDao(db: HouseShareDatabase): ExpenseDao = db.expenseDao()

    @Provides
    @Singleton
    fun provideUserDao(db: HouseShareDatabase): UserDao = db.userDao()

    @Provides
    @Singleton
    fun provideGroupDao(db: HouseShareDatabase): GroupDao = db.groupDao()

    @Provides
    @Singleton
    fun provideCheckoffStateDao(db: HouseShareDatabase): CheckoffStateDao = db.checkoffStateDao()

    @Provides
    @Singleton
    fun provideRetrofit(): Retrofit = Retrofit.Builder()
        .baseUrl(BuildConfig.BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    @Provides
    @Singleton
    fun provideUserApi(retrofit: Retrofit): UserApi = retrofit.create<UserApi>()

    @Provides
    @Singleton
    fun provideGroupApi(retrofit: Retrofit): GroupApi = retrofit.create<GroupApi>()
}