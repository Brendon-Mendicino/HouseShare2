package lol.terabrendon.houseshare2.di

import android.app.Application
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.room.Room
import androidx.room.RoomDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import lol.terabrendon.houseshare2.HouseShareApplication
import lol.terabrendon.houseshare2.data.local.dao.ExpenseDao
import lol.terabrendon.houseshare2.data.local.dao.GroupDao
import lol.terabrendon.houseshare2.data.local.dao.ShoppingItemDao
import lol.terabrendon.houseshare2.data.local.dao.UserDao
import lol.terabrendon.houseshare2.data.local.database.HouseShareDatabase
import lol.terabrendon.houseshare2.data.local.preferences.UserData
import lol.terabrendon.houseshare2.data.local.preferences.userPreferencesStore
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
    fun provideUserPreferencesStore(@ApplicationContext applicationContext: Context): DataStore<UserData> =
        applicationContext.userPreferencesStore

    @Provides
    @Singleton
    fun provideHouseShareDatabase(@ApplicationContext applicationContext: Context): HouseShareDatabase =
        Room
            .databaseBuilder(
                applicationContext, HouseShareDatabase::class.java, "house_share_db"
            )
//            .fallbackToDestructiveMigration(true)
//            .setQueryCallback({ sqlQuery, bindArgs ->
//                Timber.i("query: $sqlQuery | args: $bindArgs")
//            }, Executors.newSingleThreadExecutor())
            .build()

    @Provides
    @Singleton
    fun provideRoomDatabase(db: HouseShareDatabase): RoomDatabase = db

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
}