package lol.terabrendon.houseshare2

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.lifecycleScope
import com.github.michaelbull.result.onFailure
import com.github.michaelbull.result.onSuccess
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import lol.terabrendon.houseshare2.domain.usecase.FinishLoginUseCase
import lol.terabrendon.houseshare2.presentation.components.LoadingOverlayScreen
import lol.terabrendon.houseshare2.ui.theme.HouseShare2Theme
import javax.inject.Inject


@AndroidEntryPoint
class DeepLinkActivity : ComponentActivity() {
    companion object {
        const val TAG = "DeepLinkActivity"
    }

    @Inject
    lateinit var finishLoginUseCase: FinishLoginUseCase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.i(TAG, "onCreate: Handling DeepLink for login.")

        val mainActivity = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }

        // Query params
        // - state
        // - session_state
        // - iss
        // - code
        val uri = intent?.data ?: throw IllegalStateException("No URI present on activity launch!")

        lifecycleScope.launch {
            finishLoginUseCase(uri)
                .onSuccess {
                    Log.i(TAG, "onCreate: successfully authenticated! username=${it.username}")

                    startActivity(mainActivity)
                }
                .onFailure {
                    Log.e(TAG, "onCreate: auth failed!", it)

                    Toast.makeText(
                        this@DeepLinkActivity,
                        R.string.failed_to_authenticate,
                        Toast.LENGTH_LONG
                    ).show()
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