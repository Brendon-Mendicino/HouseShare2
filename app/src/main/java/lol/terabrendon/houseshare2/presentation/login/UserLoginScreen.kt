package lol.terabrendon.houseshare2.presentation.login

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope.SlideDirection.Companion.Up
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import kotlinx.coroutines.delay
import lol.terabrendon.houseshare2.presentation.components.LoadingOverlayScreen
import lol.terabrendon.houseshare2.presentation.navigation.MainNavigation
import lol.terabrendon.houseshare2.presentation.provider.RegisterTopBarConfig
import lol.terabrendon.houseshare2.presentation.provider.TopBarConfig
import lol.terabrendon.houseshare2.presentation.vm.LoginViewModel
import lol.terabrendon.houseshare2.util.ObserveAsEvent
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sin

@Composable
fun UserLoginScreen(
    modifier: Modifier = Modifier,
    viewModel: LoginViewModel = hiltViewModel(),
    onFinish: () -> Unit,
) {
    var loginIsPending by rememberSaveable { mutableStateOf(false) }
    var loginIsError by rememberSaveable { mutableStateOf(false) }

    ObserveAsEvent(viewModel.uiEvent) { event ->
        loginIsPending = false
        loginIsError = false

        when (event) {
            LoginUiEvent.LoginSuccessful -> onFinish()
            LoginUiEvent.LoginFailed -> loginIsError = true
        }
    }

    RegisterTopBarConfig<MainNavigation.Login>(config = TopBarConfig(navigationIcon = {}))

    UserLoginInner(
        modifier = modifier,
        isPending = loginIsPending,
        isError = loginIsError,
        onEvent = viewModel::onEvent,
        onLogin = { loginIsPending = true })
}

@Composable
private fun UserLoginInner(
    modifier: Modifier = Modifier,
    onEvent: (LoginEvent) -> Unit = {},
    onLogin: () -> Unit = {},
    isPending: Boolean = false,
    isError: Boolean = false,
) {
    if (isPending) {
        LoadingOverlayScreen()
    }

    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        OutlinedCard {
            Column(
                modifier = Modifier.padding(48.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Welcome to", style = MaterialTheme.typography.titleLarge)

                Row {
                    Text("HouseShare! ", style = MaterialTheme.typography.displaySmall)
                    AnimatedCleaningEmojis()
                }

                Spacer(modifier = Modifier.requiredHeight(48.dp))

                ElevatedButton(
                    onClick = {
                        onEvent(LoginEvent.Login)
                        onLogin()
                    },
                ) {
                    Text("Login")
                }

                if (isError) {
                    Spacer(modifier = Modifier.requiredHeight(28.dp))

                    Text(
                        "An error happened during the login",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
private fun AnimatedCleaningEmojis(modifier: Modifier = Modifier) {
    val cleaningEmojis = listOf(
        "🧹", "🧼", "🧽", "🪣", "🧺", "🧴", "🧯",
        "🪠", "🪤", "🚿", "🚽", "🚰", "🪞", "🪟",
        "💧", "✨", "🛒",
    )
    var x = 0.0

    var currentEmoji by remember { mutableStateOf(cleaningEmojis.random()) }

    LaunchedEffect(Unit) {
        val period = PI
        val step = PI / 25

        while (true) {
            delay(500L)
            x = (x + step) % period
        }
    }

    LaunchedEffect(Unit) {
        val s = { x: Double -> abs(sin(x).pow(8) - 1) }
        val g = { x: Double -> s(x) + 2.0.pow(-s(x) / 8) - 2.0.pow(-s(0.0) / 8) }
        val f = { x: Double -> (g(x) * 1000).toLong() }

        while (true) {
            delay(f(x))
            while (true) {
                val newEmoji = cleaningEmojis.random()
                if (newEmoji == currentEmoji) continue
                currentEmoji = newEmoji
                break
            }
        }
    }

    AnimatedContent(
        targetState = currentEmoji,
        transitionSpec = {
            slideIntoContainer(towards = Up) + fadeIn() togetherWith slideOutOfContainer(towards = Up) + fadeOut()
        }) { icon ->
        Text(icon, modifier = modifier, style = MaterialTheme.typography.displaySmall)
    }
}


@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun LoginPreview() {
    UserLoginInner(isError = true)
}