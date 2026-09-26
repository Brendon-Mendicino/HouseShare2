package lol.terabrendon.houseshare2.presentation.screen.login

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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import kotlinx.coroutines.delay
import lol.terabrendon.houseshare2.R
import lol.terabrendon.houseshare2.domain.form.LoginFormState
import lol.terabrendon.houseshare2.domain.form.LoginFormStateValidator
import lol.terabrendon.houseshare2.domain.form.toValidator
import lol.terabrendon.houseshare2.presentation.components.FormOutlinedTextField
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
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun UserLoginScreen(
    modifier: Modifier = Modifier,
    viewModel: LoginViewModel = hiltViewModel(),
    onFinish: () -> Unit,
) {
    var loginIsError by rememberSaveable { mutableStateOf(false) }
    val formState by viewModel.formState.collectAsState()
    val isPending by viewModel.isPending.collectAsState()

    ObserveAsEvent(viewModel.uiEvent) { event ->
        loginIsError = false

        when (event) {
            LoginUiEvent.LoginSuccessful -> onFinish()
            LoginUiEvent.LoginFailed -> loginIsError = true
        }
    }

    RegisterTopBarConfig(
        config = TopBarConfig(navigationIcon = {}),
        route = MainNavigation.Login::class,
    )

    UserLoginInner(
        modifier = modifier,
        formState = formState,
        isPending = isPending,
        isError = loginIsError,
        onEvent = viewModel::onEvent,
    )
}

@Composable
private fun UserLoginInner(
    modifier: Modifier = Modifier,
    formState: LoginFormStateValidator = LoginFormState().toValidator(),
    onEvent: (LoginEvent) -> Unit = {},
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

                FormOutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    param = formState.username,
                    onValueChange = { onEvent(LoginEvent.UsernameChanged(it)) },
                    labelText = stringResource(R.string.username),
                    maxLines = 1,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                )

                Spacer(modifier = Modifier.requiredHeight(16.dp))

                FormOutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    param = formState.password,
                    onValueChange = { onEvent(LoginEvent.PasswordChanged(it)) },
                    labelText = stringResource(R.string.password),
                    maxLines = 1,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Done,
                        keyboardType = KeyboardType.Password,
                    ),
                    keyboardActions = KeyboardActions(onDone = { onEvent(LoginEvent.Login) }),
                )

                Spacer(modifier = Modifier.requiredHeight(24.dp))

                ElevatedButton(
                    onClick = { onEvent(LoginEvent.Login) },
                ) {
                    Text(stringResource(R.string.login))
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

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
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
            delay(500L.milliseconds)
            x = (x + step) % period
        }
    }

    LaunchedEffect(Unit) {
        val s = { x: Double -> abs(sin(x).pow(8) - 1) }
        val g = { x: Double -> (s(x) + 0.3) / 1.3 }
        val f = { x: Double -> (g(x) * 1000).toLong() }

        while (true) {
            delay(f(x).milliseconds)
            while (true) {
                val newEmoji = cleaningEmojis.random()
                if (newEmoji == currentEmoji) continue
                currentEmoji = newEmoji
                break
            }
        }
    }

    val slide = MaterialTheme.motionScheme.fastSpatialSpec<IntOffset>()
    val fade = MaterialTheme.motionScheme.fastEffectsSpec<Float>()

    AnimatedContent(
        targetState = currentEmoji,
        transitionSpec = {
            slideIntoContainer(
                animationSpec = slide,
                towards = Up
            ) + fadeIn(fade) togetherWith slideOutOfContainer(
                animationSpec = slide,
                towards = Up
            ) + fadeOut(fade)
        }) { icon ->
        Text(icon, modifier = modifier, style = MaterialTheme.typography.displaySmall)
    }
}


@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun LoginPreview() {
    UserLoginInner(isError = true)
}