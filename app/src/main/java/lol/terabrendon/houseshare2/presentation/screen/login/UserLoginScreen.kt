package lol.terabrendon.houseshare2.presentation.screen.login

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope.SlideDirection.Companion.Up
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
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
import timber.log.Timber
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
    val uriHandler = LocalUriHandler.current

    ObserveAsEvent(viewModel.uiEvent) { event ->
        loginIsError = false

        when (event) {
            LoginUiEvent.LoginSuccessful -> onFinish()
            LoginUiEvent.LoginFailed -> loginIsError = true
            // The account is created in the browser, the user comes back here to log in.
            is LoginUiEvent.OpenRegistration -> runCatching { uriHandler.openUri(event.url) }
                .onFailure { Timber.e(it, "UserLoginScreen: no browser to open the registration") }
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
    var passwordVisible by rememberSaveable { mutableStateOf(false) }

    if (isPending) {
        LoadingOverlayScreen()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .imePadding()
            .verticalScroll(rememberScrollState()),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 420.dp)
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text("Welcome to", style = MaterialTheme.typography.titleLarge)

            Row {
                Text("HouseShare! ", style = MaterialTheme.typography.displaySmall)
                AnimatedCleaningEmojis()
            }

            Spacer(modifier = Modifier.requiredHeight(40.dp))

            FormOutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                param = formState.username,
                onValueChange = { onEvent(LoginEvent.UsernameChanged(it)) },
                labelText = stringResource(R.string.username),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            )

            Spacer(modifier = Modifier.requiredHeight(12.dp))

            FormOutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                param = formState.password,
                onValueChange = { onEvent(LoginEvent.PasswordChanged(it)) },
                labelText = stringResource(R.string.password),
                singleLine = true,
                visualTransformation = if (passwordVisible) VisualTransformation.None
                else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Done,
                    keyboardType = KeyboardType.Password,
                ),
                keyboardActions = KeyboardActions(onDone = { onEvent(LoginEvent.Login) }),
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Filled.VisibilityOff
                            else Icons.Filled.Visibility,
                            contentDescription = stringResource(
                                if (passwordVisible) R.string.hide_password
                                else R.string.show_password
                            ),
                        )
                    }
                },
            )

            AnimatedVisibility(visible = isError) {
                Text(
                    text = stringResource(R.string.login_error),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }

            Spacer(modifier = Modifier.requiredHeight(24.dp))

            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = { onEvent(LoginEvent.Login) },
            ) {
                Text(stringResource(R.string.login))
            }

            Row(
                modifier = Modifier.padding(vertical = 24.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                HorizontalDivider(modifier = Modifier.weight(1f))
                Text(
                    text = stringResource(R.string.or),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                HorizontalDivider(modifier = Modifier.weight(1f))
            }

            Text(
                text = stringResource(R.string.new_to_houseshare),
                style = MaterialTheme.typography.titleMedium,
            )

            Spacer(modifier = Modifier.requiredHeight(12.dp))

            OutlinedButton(
                modifier = Modifier.fillMaxWidth(),
                onClick = { onEvent(LoginEvent.Register) },
            ) {
                Text(stringResource(R.string.create_account))
                Spacer(modifier = Modifier.size(ButtonDefaults.IconSpacing))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                    contentDescription = null,
                    modifier = Modifier.size(ButtonDefaults.IconSize),
                )
            }

            Spacer(modifier = Modifier.requiredHeight(8.dp))

            Text(
                text = stringResource(R.string.register_in_browser_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
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
