package lol.terabrendon.houseshare2

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import lol.terabrendon.houseshare2.data.local.preferences.UserData
import lol.terabrendon.houseshare2.presentation.home.HouseShareMain
import lol.terabrendon.houseshare2.presentation.util.ActivityQueue
import lol.terabrendon.houseshare2.presentation.vm.MainViewModel
import lol.terabrendon.houseshare2.ui.theme.HouseShare2Theme

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        println("BuildConfig.DEBUG=${BuildConfig.DEBUG}")

        enableEdgeToEdge()

        lifecycleScope.launch {
            ActivityQueue.intents.collect {
                startActivity(it)
            }
        }

        setContent {
            val theme by viewModel.theme.collectAsStateWithLifecycle()
            val dynamicColor by viewModel.dynamicColor.collectAsStateWithLifecycle()
            val darkTheme = when (theme) {
                UserData.Theme.System -> isSystemInDarkTheme()
                UserData.Theme.Light -> false
                UserData.Theme.Dark -> true
            }

            HouseShare2Theme(darkTheme = darkTheme, dynamicColor = dynamicColor) {
                HouseShareMain(viewModel)
            }
        }
    }
}
