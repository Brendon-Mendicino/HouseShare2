package lol.terabrendon.houseshare2.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import lol.terabrendon.houseshare2.UserPreferences
import lol.terabrendon.houseshare2.dao.ExpenseDao
import lol.terabrendon.houseshare2.dao.ShoppingItemDao
import lol.terabrendon.houseshare2.dao.UserDao
import lol.terabrendon.houseshare2.database.HouseShareDatabase
import lol.terabrendon.houseshare2.preferences.userPreferencesStore
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

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
}