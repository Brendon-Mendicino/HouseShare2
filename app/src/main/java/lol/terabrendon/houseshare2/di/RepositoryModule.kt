package lol.terabrendon.houseshare2.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import lol.terabrendon.houseshare2.repository.UserPreferencesRepository
import lol.terabrendon.houseshare2.repository.UserPreferencesRepositoryImpl
import javax.inject.Singleton

@Suppress("unused")
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindUserPreferencesRepository(
        repo: UserPreferencesRepositoryImpl
    ): UserPreferencesRepository
}