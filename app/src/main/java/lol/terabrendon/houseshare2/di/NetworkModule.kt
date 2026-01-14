package lol.terabrendon.houseshare2.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import jakarta.inject.Singleton
import lol.terabrendon.houseshare2.data.util.ConnectivityNetworkMonitor
import lol.terabrendon.houseshare2.data.util.NetworkMonitor

@Module
@InstallIn(SingletonComponent::class)
abstract class NetworkModule {
    @Binds
    @Singleton
    abstract fun bindNetworkMonitor(
        impl: ConnectivityNetworkMonitor,
    ): NetworkMonitor
}
