package lol.terabrendon.houseshare2.presentation.vm

import android.content.Intent
import android.util.Log
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import lol.terabrendon.houseshare2.data.remote.api.LoginApi
import lol.terabrendon.houseshare2.data.repository.UserPreferencesRepository
import lol.terabrendon.houseshare2.presentation.navigation.MainNavigation
import lol.terabrendon.houseshare2.presentation.util.ActivityQueue
import javax.inject.Inject
import kotlin.reflect.KClass

@HiltViewModel
class MainViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val loginApi: LoginApi,
) : ViewModel() {
    companion object {
        private const val TAG = "MainViewModel"
    }

    init {
        viewModelScope.launch {
            try {
                println("loginApi.login")
                val res = loginApi.login()
                val url = res.headers()["Location"]!!

                val uri = url.toUri()
//                    val redirect = uri.getQueryParameter("redirect_uri")!!.toUri().setScheme("app")
//                        .setAuthority("lol.terabrendon.houseshare2")

                val intent = Intent(Intent.ACTION_VIEW, uri)
                ActivityQueue.activities.emit(intent)
            } catch (e: Exception) {
                println(e.message)
            }
        }
    }

    val currentNavigation = userPreferencesRepository
        .savedDestination
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), MainNavigation.Loading)

    val startingDestination = userPreferencesRepository
        .savedDestination
        .take(1)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), MainNavigation.Loading)

    val topLevelRoutes = userPreferencesRepository
        .topLevelRoutes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    fun setCurrentNavigation(destination: KClass<out MainNavigation>) {
        viewModelScope.launch {
            Log.i(TAG, "setCurrentNavigation: new destination: $destination")
            userPreferencesRepository.updateMainDestination(destination)
        }
    }
}