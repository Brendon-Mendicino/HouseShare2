package lol.terabrendon.houseshare2.presentation.vm

import android.content.Intent
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import lol.terabrendon.houseshare2.data.remote.api.LoginApi
import lol.terabrendon.houseshare2.presentation.login.LoginEvent
import lol.terabrendon.houseshare2.presentation.util.ActivityQueue
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginApi: LoginApi,
) : ViewModel() {

    fun onEvent(event: LoginEvent) {
        when (event) {
            LoginEvent.Login -> viewModelScope.launch {
                try {
                    println("loginApi.login")
                    val res = loginApi.login()
                    val url = res.headers()["Location"]!!

                    val uri = url.toUri()

                    val intent = Intent(Intent.ACTION_VIEW, uri)
                    ActivityQueue.activities.emit(intent)
                } catch (e: Exception) {
                    println(e.message)
                }
            }
        }
    }
}