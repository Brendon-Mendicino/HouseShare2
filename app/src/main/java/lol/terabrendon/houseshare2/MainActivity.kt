package lol.terabrendon.houseshare2

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
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
            ActivityQueue.activities.collect {
                startActivity(it)
            }
        }

        setContent {
            HouseShare2Theme {
                HouseShareMain(viewModel)
            }
        }
    }
}
