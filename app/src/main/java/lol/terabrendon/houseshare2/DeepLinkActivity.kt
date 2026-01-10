package lol.terabrendon.houseshare2

import android.content.Intent
import android.net.Uri
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
import lol.terabrendon.houseshare2.domain.usecase.AcceptInviteUseCase
import lol.terabrendon.houseshare2.domain.usecase.FinishLoginUseCase
import lol.terabrendon.houseshare2.domain.usecase.FinishLogoutUseCase
import lol.terabrendon.houseshare2.presentation.components.LoadingOverlayScreen
import lol.terabrendon.houseshare2.ui.theme.HouseShare2Theme
import lol.terabrendon.houseshare2.util.matcher
import javax.inject.Inject


@AndroidEntryPoint
class DeepLinkActivity : ComponentActivity() {
    companion object {
        const val TAG = "DeepLinkActivity"
    }

    @Inject
    lateinit var finishLoginUseCase: FinishLoginUseCase

    @Inject
    lateinit var finishLogoutUseCase: FinishLogoutUseCase

    @Inject
    lateinit var acceptInviteUseCase: AcceptInviteUseCase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.i(TAG, "onCreate: Handling DeepLink for login.")

        val uri = intent?.data ?: throw IllegalStateException("No URI present on activity launch!")

        lifecycleScope.launch { handleLinks(uri) }

        setContent {
            HouseShare2Theme {
                LoadingOverlayScreen()
            }
        }
    }

    suspend fun handleLinks(uri: Uri) {
        val mainActivity = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }

        matcher(
            uri.path ?: "",
            "^/logout$" to { logout(mainActivity) },
            "^/login/oauth2$" to { login(uri, mainActivity) },
            """^\/api\/v\d+\/groups\/(?<groupId>[^\/]+)\/invite\/join$""" to {
                val groupId = it.groups[1]?.value?.toLongOrNull()!!
                Log.i(TAG, "onCreate: matching invite groupId=$groupId")
                groupInvite(groupId, uri, mainActivity)
            }
        ) {
            val msg = "onCreate: invalid uri path being matched! uri.path=${uri.path}"
            Log.e(TAG, msg)
            throw IllegalStateException(msg)
        }

        finish()
    }

    suspend fun logout(mainActivity: Intent) {
        finishLogoutUseCase()
        startActivity(mainActivity)
    }

    suspend fun login(uri: Uri, mainActivity: Intent) {
        finishLoginUseCase(uri)
            .onSuccess {
                Log.i(TAG, "onCreate: successfully authenticated! username=${it.username}")

                startActivity(mainActivity)
            }
            .onFailure {
                Log.e(TAG, "onCreate: auth failed! error=$it")

                Toast.makeText(
                    this@DeepLinkActivity,
                    R.string.failed_to_authenticate,
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    suspend fun groupInvite(groupId: Long, uri: Uri, mainActivity: Intent) {
        acceptInviteUseCase(groupId, uri)
            .onSuccess {
                Log.i(TAG, "groupInvite: successfully accepted group invite! groupId=$groupId")
            }
            .onFailure {
                Log.w(TAG, "groupInvite: group invite failed! error=$it")

                Toast.makeText(
                    this@DeepLinkActivity,
                    getString(R.string.invite_link_was_not_valid),
                    Toast.LENGTH_LONG
                ).show()
            }

        startActivity(mainActivity)
    }
}