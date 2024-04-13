package lol.terabrendon.houseshare2.di

import android.content.Context
import androidx.datastore.core.DataStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import lol.terabrendon.houseshare2.UserPreferences
import lol.terabrendon.houseshare2.preferences.userPreferencesStore
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideUserPreferencesStore(@ApplicationContext applicationContext: Context): DataStore<UserPreferences> =
        applicationContext.userPreferencesStore
}