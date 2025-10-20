package lol.terabrendon.houseshare2

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import lol.terabrendon.houseshare2.data.api.LoginApi
import lol.terabrendon.houseshare2.presentation.components.LoadingOverlayScreen
import lol.terabrendon.houseshare2.ui.theme.HouseShare2Theme
import lol.terabrendon.houseshare2.util.setAuthority
import lol.terabrendon.houseshare2.util.setPath
import lol.terabrendon.houseshare2.util.setScheme
import retrofit2.HttpException
import javax.inject.Inject


@AndroidEntryPoint
class DeepLinkActivity : ComponentActivity() {
    companion object {
        const val TAG = "DeepLinkActivity"
    }

    @Inject
    lateinit var loginApi: LoginApi

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.i(TAG, "onCreate: Handling DeepLink for login.")

        val mainActivity = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }

        val uri = intent?.data ?: throw IllegalStateException("No URI present on activity launch!")

        lifecycleScope.launch {
            val serverUrl = uri.setScheme("http").setAuthority("10.0.2.2", 9090)
                .setPath("login/oauth2/code/house-share-client")

            try {
                loginApi.authCodeFlow(serverUrl.toString())
                Log.i(TAG, "onCreate: successfully authenticated!")
                startActivity(mainActivity)
            } catch (e: HttpException) {
                Toast.makeText(
                    this@DeepLinkActivity,
                    R.string.failed_to_authenticate,
                    Toast.LENGTH_LONG
                ).show()

                Log.e(TAG, "onCreate: auth failed!", e)
            }

            finish()
        }

        setContent {
            HouseShare2Theme {
                LoadingOverlayScreen()
            }
        }
    }
}