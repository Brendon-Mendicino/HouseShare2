package lol.terabrendon.houseshare2.presentation.settings

import android.content.ClipData
import android.os.Build
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType.Companion.PrimaryNotEditable
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.toClipEntry
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.os.LocaleListCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import lol.terabrendon.houseshare2.BuildConfig
import lol.terabrendon.houseshare2.R
import lol.terabrendon.houseshare2.data.local.preferences.UserData
import lol.terabrendon.houseshare2.data.repository.UserDataRepository
import lol.terabrendon.houseshare2.data.repository.UserDataRepository.Update.DynamicColor
import lol.terabrendon.houseshare2.data.repository.UserDataRepository.Update.SendAnalytics
import lol.terabrendon.houseshare2.data.repository.UserDataRepository.Update.Theme
import lol.terabrendon.houseshare2.presentation.util.SnackbarController
import lol.terabrendon.houseshare2.presentation.util.SnackbarEvent
import lol.terabrendon.houseshare2.presentation.util.UiText
import lol.terabrendon.houseshare2.presentation.vm.SettingsViewModel
import lol.terabrendon.houseshare2.ui.theme.HouseShare2Theme
import lol.terabrendon.houseshare2.util.Config

@Composable
fun SettingsScreen(modifier: Modifier = Modifier, viewModel: SettingsViewModel = hiltViewModel()) {
    val userData by viewModel.userSettings.collectAsStateWithLifecycle()

    SettingsInner(
        modifier = modifier,
        userData = userData,
        onEvent = viewModel::onEvent,
    )
}

@Composable
private fun SettingsInner(
    modifier: Modifier = Modifier,
    userData: UserData,
    onEvent: (UserDataRepository.Update) -> Unit,
) {
    val clipboard = LocalClipboard.current
    val uriHandler = LocalUriHandler.current
    val appName = stringResource(R.string.app_name)
    val scope = rememberCoroutineScope()

    val localeOptions = mapOf(
        "English" to "en",
        "Italiano" to "it",
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        SettingsHeader(stringResource(R.string.general))
        LanguagePicker(localeOptions = localeOptions)
        ThemePicker(theme = userData.theme, onClick = { onEvent(Theme(it)) })
        DynamicColorPicker(
            dynamic = userData.dynamicColor,
            toggle = { onEvent(DynamicColor(it)) },
        )

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))


        // Privacy & Safety
        SettingsHeader(stringResource(R.string.privacy_safety))

        // Privacy / Crashlytics Opt-In (GDPR compliant)
        ListItem(
            headlineContent = { Text(stringResource(R.string.help_us_improve)) },
            supportingContent = {
                Text(stringResource(R.string.send_analytics))
            },
            trailingContent = {
                Switch(
                    checked = userData.sendAnalytics,
                    onCheckedChange = { onEvent(SendAnalytics(userData.sendAnalytics.not())) }
                )
            }
        )

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        // About & Legal
        SettingsHeader(stringResource(R.string.about))
        ListItem(
            modifier = Modifier.clickable { uriHandler.openUri(Config.PRIVACY_URL) },
            headlineContent = { Text(stringResource(R.string.privacy_policies)) },
            leadingContent = { Icon(Icons.Default.Shield, null) },
            trailingContent = {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                    contentDescription = stringResource(R.string.open_in_browser),
                )
            }
        )

        ListItem(
            modifier = Modifier.clickable { uriHandler.openUri(Config.TERMS_URL) },
            headlineContent = { Text(stringResource(R.string.terms_conditions)) },
            leadingContent = { Icon(Icons.Default.Gavel, null) },
            trailingContent = { Icon(Icons.AutoMirrored.Filled.OpenInNew, null) }
        )

        ListItem(
            headlineContent = { Text(stringResource(R.string.version)) },
            supportingContent = {
                Text("${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})")
            },
            leadingContent = { Icon(Icons.Default.Info, null) },
            modifier = Modifier.combinedClickable(
                onClick = {
                    scope.launch {
                        SnackbarController.sendEvent(
                            SnackbarEvent(
                                message = UiText.Res(R.string.keep_pressed_to_copy_the_version)
                            )
                        )
                    }
                },
                onLongClick = {
                    // Helpful for bug reporting: copy build info to clipboard
                    val buildInfo =
                        "$appName v${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})"
                    val entry = ClipData.newPlainText("Version", buildInfo).toClipEntry()

                    scope.launch {
                        clipboard.setClipEntry(entry)
                        SnackbarController.sendEvent(
                            SnackbarEvent(
                                message = UiText.Res(R.string.version_copied_to_clipboard)
                            )
                        )
                    }
                }
            )
        )

        ListItem(
            headlineContent = { Text(stringResource(R.string.environment)) },
            supportingContent = {
                Text(BuildConfig.BUILD_TYPE)
            },
            leadingContent = { Icon(Icons.Default.Terminal, null) }
        )

    }
}

@Composable
private fun SettingsHeader(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 16.dp, bottom = 8.dp, top = 16.dp)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LanguagePicker(modifier: Modifier = Modifier, localeOptions: Map<String, String>) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
    ) {
        OutlinedTextField(
            readOnly = true,
            modifier = modifier
                .fillMaxWidth()
                .animateContentSize()
                .menuAnchor(PrimaryNotEditable),
            value = stringResource(R.string.current_language),
            label = { Text(stringResource(R.string.language)) },
            onValueChange = { },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(
                    expanded = expanded
                )
            }
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = {
                expanded = false
            }
        ) {
            localeOptions.keys.forEach { selectionLocale ->
                DropdownMenuItem(
                    onClick = {
                        expanded = false
                        // set app locale given the user's selected locale
                        AppCompatDelegate.setApplicationLocales(
                            LocaleListCompat.forLanguageTags(
                                localeOptions[selectionLocale]
                            )
                        )
                    },
                    text = { Text(selectionLocale) },
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ThemePicker(
    modifier: Modifier = Modifier,
    theme: UserData.Theme,
    onClick: (UserData.Theme) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val themes = mapOf(
        UserData.Theme.System to stringResource(R.string.system),
        UserData.Theme.Light to stringResource(R.string.light),
        UserData.Theme.Dark to stringResource(R.string.dark),
    )

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
    ) {
        OutlinedTextField(
            readOnly = true,
            modifier = modifier
                .fillMaxWidth()
                .animateContentSize()
                .menuAnchor(PrimaryNotEditable),
            value = themes[theme]!!,
            label = { Text(stringResource(R.string.application_theme)) },
            onValueChange = { },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(
                    expanded = expanded
                )
            }
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = {
                expanded = false
            }
        ) {
            themes.forEach { (value, text) ->
                DropdownMenuItem(
                    onClick = {
                        expanded = false
                        onClick(value)
                    },
                    text = { Text(text) },
                )
            }
        }
    }
}

@Composable
private fun DynamicColorPicker(
    modifier: Modifier = Modifier,
    dynamic: Boolean,
    toggle: (Boolean) -> Unit,
) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
        return
    }

    ListItem(
        modifier = modifier,
        headlineContent = { Text(stringResource(R.string.dynamic_colors)) },
        supportingContent = {
            Text(stringResource(R.string.dynamic_colors_description))
        },
        trailingContent = {
            Switch(
                checked = dynamic,
                onCheckedChange = { toggle(!dynamic) },
            )
        }
    )
}

@Preview(showBackground = true)
@Composable
private fun SettingsPreview() {
    HouseShare2Theme {
        SettingsInner(userData = UserData(), onEvent = {})
    }
}