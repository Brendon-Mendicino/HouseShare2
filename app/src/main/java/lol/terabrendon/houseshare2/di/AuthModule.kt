package lol.terabrendon.houseshare2.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import lol.terabrendon.houseshare2.data.remote.idp.KeycloakAuthenticator
import lol.terabrendon.houseshare2.domain.auth.IdpAuthenticator
import javax.inject.Singleton

/**
 * Changing identity provider means writing another [IdpAuthenticator] and changing this binding.
 */
@Suppress("unused")
@Module
@InstallIn(SingletonComponent::class)
abstract class AuthModule {
    @Binds
    @Singleton
    abstract fun bindIdpAuthenticator(authenticator: KeycloakAuthenticator): IdpAuthenticator
}
