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
import lol.terabrendon.houseshare2.data.local.dao.GroupMemberDao
import lol.terabrendon.houseshare2.data.local.dao.ShoppingItemDao
import lol.terabrendon.houseshare2.data.local.dao.UserDao
import lol.terabrendon.houseshare2.data.local.database.HouseShareDatabaseV2
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
    fun provideHouseShareDatabase(@ApplicationContext applicationContext: Context): HouseShareDatabaseV2 =
        Room
            .databaseBuilder(
                applicationContext, HouseShareDatabaseV2::class.java, "house_share_db_v2"
            )
//            .addMigrations(MIGRATION_1_2)
            .fallbackToDestructiveMigration(true)
//            .setQueryCallback({ sqlQuery, bindArgs ->
//                Timber.i("query: $sqlQuery | args: $bindArgs")
//            }, Executors.newSingleThreadExecutor())
            .build()

    @Provides
    @Singleton
    fun provideRoomDatabase(db: HouseShareDatabaseV2): RoomDatabase = db

    @Provides
    @Singleton
    fun provideShoppingItemDao(db: HouseShareDatabaseV2): ShoppingItemDao = db.shoppingItemDao()

    @Provides
    @Singleton
    fun provideExpenseDao(db: HouseShareDatabaseV2): ExpenseDao = db.expenseDao()

    @Provides
    @Singleton
    fun provideUserDao(db: HouseShareDatabaseV2): UserDao = db.userDao()

    @Provides
    @Singleton
    fun provideGroupDao(db: HouseShareDatabaseV2): GroupDao = db.groupDao()

    @Provides
    @Singleton
    fun provideGroupMemberDao(db: HouseShareDatabaseV2): GroupMemberDao = db.groupMemberDao()
}