package lol.terabrendon.houseshare2.presentation.settings

import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType.Companion.PrimaryNotEditable
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.os.LocaleListCompat
import lol.terabrendon.houseshare2.R
import lol.terabrendon.houseshare2.ui.theme.HouseShare2Theme

@Composable
fun SettingsScreen(modifier: Modifier = Modifier) {

    SettingsInner(modifier = modifier)
}

@Composable
private fun SettingsInner(modifier: Modifier = Modifier) {
    val localeOptions = mapOf(
        "English" to "en",
        "Italiano" to "it",
    )

    Column(modifier = modifier.padding(8.dp)) {
        LanguagePicker(localeOptions = localeOptions)
    }
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
            modifier = Modifier
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

@Preview(showBackground = true)
@Composable
private fun SettingsPreview(modifier: Modifier = Modifier) {
    HouseShare2Theme {
        SettingsInner()
    }
}