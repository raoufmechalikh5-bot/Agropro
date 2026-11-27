package com.example.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.LocaleHelper
import com.example.R
import com.example.data.local.ThemePreference
import kotlinx.coroutines.launch

@Composable
fun AgroProSettingsDialog(
    themePreference: ThemePreference,
    onThemeChange: suspend (ThemePreference) -> Unit,
    onLanguageChange: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val currentLanguage = LocaleHelper.getLanguage(context)
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.settings_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(stringResource(R.string.settings_appearance), style = MaterialTheme.typography.titleSmall)
                listOf(
                    ThemePreference.SYSTEM to R.string.settings_theme_system,
                    ThemePreference.LIGHT to R.string.settings_theme_light,
                    ThemePreference.DARK to R.string.settings_theme_dark
                ).forEach { (choice, label) ->
                    Row(
                        modifier = Modifier.fillMaxWidth().clickable {
                            scope.launch { onThemeChange(choice) }
                        }.padding(vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = themePreference == choice,
                            onClick = { scope.launch { onThemeChange(choice) } }
                        )
                        Text(stringResource(label))
                    }
                }
                Text(stringResource(R.string.settings_language), style = MaterialTheme.typography.titleSmall)
                listOf(
                    "ar" to R.string.settings_language_ar,
                    "fr" to R.string.settings_language_fr,
                    "en" to R.string.settings_language_en
                ).forEach { (code, label) ->
                    Row(
                        modifier = Modifier.fillMaxWidth().clickable {
                            onLanguageChange(code)
                        }.padding(vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = currentLanguage == code,
                            onClick = { onLanguageChange(code) }
                        )
                        Text(stringResource(label))
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.settings_close))
            }
        }
    )
}
