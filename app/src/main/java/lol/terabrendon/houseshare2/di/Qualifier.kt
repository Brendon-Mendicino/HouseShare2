package lol.terabrendon.houseshare2.di

import javax.inject.Qualifier

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class IoDispatcher

/**
 * The client used to talk to the identity provider. It shares the cookie jar with the other
 * clients, the provider session is stored exactly like the server one.
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class IdpRetrofit
