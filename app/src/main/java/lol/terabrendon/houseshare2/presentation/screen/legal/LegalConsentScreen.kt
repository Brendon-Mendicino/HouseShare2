package lol.terabrendon.houseshare2.presentation.screen.legal

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import lol.terabrendon.houseshare2.R
import lol.terabrendon.houseshare2.presentation.navigation.MainNavigation
import lol.terabrendon.houseshare2.presentation.provider.RegisterTopBarConfig
import lol.terabrendon.houseshare2.presentation.provider.TopBarConfig
import lol.terabrendon.houseshare2.presentation.screen.settings.LanguagePicker
import lol.terabrendon.houseshare2.presentation.vm.LegalViewModel
import lol.terabrendon.houseshare2.ui.theme.HouseShare2Theme
import lol.terabrendon.houseshare2.util.Config

@Composable
fun LegalConsentScreen(modifier: Modifier = Modifier, viewModel: LegalViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    RegisterTopBarConfig<MainNavigation.Legal>(config = TopBarConfig(navigationIcon = {}))

    LegalConsentInner(modifier = modifier, state = state, onEvent = viewModel::onEvent)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LegalConsentInner(
    modifier: Modifier = Modifier,
    onEvent: (LegalViewModel.Event) -> Unit,
    state: LegalViewModel.UiState,
) {
    val uriHandler = LocalUriHandler.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header Area
        Icon(
            imageVector = Icons.Default.Gavel,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.terms_and_privacy),
            style = MaterialTheme.typography.headlineMedium
        )
        Text(
            text = stringResource(R.string.please_review_and_accept_the_terms),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(32.dp))

        // Terms of Service Link & Check
        ListItem(
            headlineContent = { Text(stringResource(R.string.terms_conditions)) },
            supportingContent = {
                TextButton(
                    onClick = {
                        uriHandler.openUri(Config.TERMS_URL)
                    },
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text(stringResource(R.string.read_our_terms_on_the_website))
                    Icon(Icons.AutoMirrored.Filled.OpenInNew, null, Modifier.size(14.dp))
                }
            },
            trailingContent = {
                Checkbox(
                    checked = state.termsAccepted,
                    onCheckedChange = { onEvent(LegalViewModel.Event.TermsAccepted) },
                )
            }
        )

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        ListItem(
            headlineContent = {
                Text(stringResource(R.string.privacy_policies))
            },
            supportingContent = {
                TextButton(
                    onClick = { uriHandler.openUri(Config.PRIVACY_URL) },
                    contentPadding = PaddingValues(0.dp),
                ) {
                    Text(stringResource(R.string.read_our_privacy_policy_on_the_website))
                    Icon(Icons.AutoMirrored.Filled.OpenInNew, null, Modifier.size(14.dp))
                }
            },
        )


        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        // Privacy / Crashlytics Opt-In (GDPR compliant)
        ListItem(
            headlineContent = { Text(stringResource(R.string.help_us_improve)) },
            supportingContent = {
                Text(stringResource(R.string.send_analytics))
            },
            trailingContent = {
                Switch(
                    checked = state.analyticsAccepted,
                    onCheckedChange = { onEvent(LegalViewModel.Event.AnalyticsToggled) }
                )
            }
        )

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        LanguagePicker()

        Spacer(Modifier.weight(1f))

        Button(
            onClick = { onEvent(LegalViewModel.Event.Finish) },
            enabled = state.termsAccepted, // User MUST accept terms to proceed
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Get Started")
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ConsentPreview() {
    HouseShare2Theme {
        LegalConsentInner(onEvent = {}, state = LegalViewModel.UiState())
    }
}