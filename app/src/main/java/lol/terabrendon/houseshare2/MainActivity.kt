package lol.terabrendon.houseshare2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import lol.terabrendon.houseshare2.presentation.home.HouseShareMain
import lol.terabrendon.houseshare2.presentation.util.ActivityQueue
import lol.terabrendon.houseshare2.ui.theme.HouseShare2Theme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // TODO: enables coroutines debugging
        println("BuildConfig.DEBUG=${BuildConfig.DEBUG}")
        System.setProperty("kotlinx.coroutines.debug", if (BuildConfig.DEBUG) "on" else "off")

        enableEdgeToEdge()

        lifecycleScope.launch {
            ActivityQueue.activities.collect {
                startActivity(it)
            }
        }

        setContent {
            HouseShare2Theme {
                HouseShareMain()
            }
        }
    }
}
