package lol.terabrendon.houseshare2.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import lol.terabrendon.houseshare2.data.repository.ExpenseRepository
import lol.terabrendon.houseshare2.data.repository.ExpenseRepositoryImpl
import lol.terabrendon.houseshare2.data.repository.ShoppingItemRepository
import lol.terabrendon.houseshare2.data.repository.ShoppingItemRepositoryImpl
import lol.terabrendon.houseshare2.data.repository.UserPreferencesRepository
import lol.terabrendon.houseshare2.data.repository.UserPreferencesRepositoryImpl
import lol.terabrendon.houseshare2.data.repository.UserRepository
import lol.terabrendon.houseshare2.data.repository.UserRepositoryImpl
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

    @Binds
    @Singleton
    abstract fun bindShoppingItemRepository(
        repo: ShoppingItemRepositoryImpl
    ): ShoppingItemRepository

    @Binds
    @Singleton
    abstract fun bindExpenseRepository(
        repo: ExpenseRepositoryImpl
    ): ExpenseRepository

    @Binds
    @Singleton
    abstract fun bindUserRepository(
        repo: UserRepositoryImpl
    ): UserRepository
}