package com.trm.daylighter.feature.settings

import android.util.Patterns
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import androidx.compose.ui.window.DialogProperties
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.jamal.composeprefs3.ui.GroupHeader
import com.jamal.composeprefs3.ui.LocalPrefsDataStore
import com.jamal.composeprefs3.ui.PrefsScreen
import com.jamal.composeprefs3.ui.prefs.*
import com.trm.daylighter.core.datastore.PreferencesDataStoreKeys
import com.trm.daylighter.core.datastore.preferencesDataStore
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import timber.log.Timber

const val settingsRoute = "settings_route"

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun SettingsScreen(modifier: Modifier = Modifier) {
  PrefsScreen(dataStore = LocalContext.current.preferencesDataStore, modifier = modifier) {
    prefsGroup({
      GroupHeader(
        title = stringResource(R.string.geocoding_pref_group_title),
        color = MaterialTheme.colorScheme.secondary
      )
    }) {
      prefsItem {
        val emailEmptyError = stringResource(id = R.string.email_empty_error)
        val invalidEmailAddress = stringResource(id = R.string.invalid_email_error)
        EditTextPref(
          key = PreferencesDataStoreKeys.GEOCODING_EMAIL,
          title = stringResource(R.string.geocoding_email_pref_title),
          summary = stringResource(R.string.geocoding_email_pref_summary),
          dialogTitle = stringResource(R.string.geocoding_email_pref_dialog_title),
          dialogMessage = stringResource(R.string.geocoding_email_pref_dialog_message),
          validateValue = {
            when {
              it.isBlank() -> emailEmptyError
              !Patterns.EMAIL_ADDRESS.matcher(it).matches() -> invalidEmailAddress
              else -> null
            }
          },
        )
      }
    }

    prefsGroup({
      GroupHeader(
        title = stringResource(R.string.application_data_pref_group_title),
        color = MaterialTheme.colorScheme.secondary
      )
    }) {
      prefsItem {
        TextPref(
          title = stringResource(R.string.clear_application_data_pref_title),
          summary = stringResource(R.string.clear_application_data_summary)
        )
      }
    }
  }
}

@ExperimentalComposeUiApi
@Composable
private fun EditTextPref(
  key: String,
  title: String,
  modifier: Modifier = Modifier,
  summary: String? = null,
  dialogTitle: String? = null,
  dialogMessage: String? = null,
  defaultValue: String = "",
  validateValue: (String) -> String? = { null },
  onValueSaved: (String) -> Unit = {},
  onValueChange: (String) -> Unit = {},
  dialogBackgroundColor: Color = MaterialTheme.colorScheme.background,
  textColor: Color = MaterialTheme.colorScheme.onBackground,
  enabled: Boolean = true,
) {
  var showDialog by rememberSaveable { mutableStateOf(false) }
  val scope = rememberCoroutineScope()

  val datastore = LocalPrefsDataStore.current
  val prefKey = stringPreferencesKey(key)
  val prefs by remember(datastore::data).collectAsState(initial = null)

  var prefValue by remember { mutableStateOf(defaultValue) }
  var textValue by rememberSaveable { mutableStateOf(prefValue) }
  var textValueChanged by rememberSaveable { mutableStateOf(false) }
  var validationMsg: String? by rememberSaveable { mutableStateOf(null) }

  LaunchedEffect(Unit) { prefs?.get(prefKey)?.also { prefValue = it } }

  LaunchedEffect(datastore.data) {
    datastore.data.collectLatest { pref -> pref[prefKey]?.also { prefValue = it } }
  }

  fun edit() {
    scope.launch {
      try {
        datastore.edit { preferences -> preferences[prefKey] = textValue }
        onValueSaved(textValue)
      } catch (ex: Exception) {
        Timber.tag("EditTextPref").e(ex, "Could not write pref $key to database.")
      }
    }
  }

  TextPref(
    title = title,
    modifier = modifier,
    summary = summary,
    textColor = textColor,
    enabled = enabled,
    onClick = { if (enabled) showDialog = !showDialog },
  )

  var dialogSize by remember { mutableStateOf(Size.Zero) }

  if (showDialog) {
    LaunchedEffect(Unit) { if (!textValueChanged) textValue = prefValue }

    AlertDialog(
      modifier = Modifier.fillMaxWidth(0.9f).onGloballyPositioned { dialogSize = it.size.toSize() },
      onDismissRequest = {
        textValueChanged = false
        textValue = prefValue
        showDialog = false
      },
      title = { DialogHeader(dialogTitle, dialogMessage) },
      text = {
        OutlinedTextField(
          value = textValue,
          modifier = Modifier.fillMaxWidth().padding(16.dp),
          placeholder = { Text(text = stringResource(R.string.geocoding_email_value_placeholder)) },
          singleLine = true,
          isError = validationMsg != null,
          supportingText = {
            AnimatedVisibility(
              visible = validationMsg != null,
              enter = fadeIn(),
              exit = fadeOut()
            ) {
              Text(text = validationMsg ?: "")
            }
          },
          onValueChange = {
            val trimmed = it.trim()
            textValue = trimmed
            validationMsg = null
            textValueChanged = true
            onValueChange(trimmed)
          }
        )
      },
      confirmButton = {
        TextButton(
          modifier = Modifier.padding(end = 16.dp),
          onClick = {
            validationMsg = validateValue(textValue)
            if (validationMsg == null) {
              edit()
              textValueChanged = false
              showDialog = false
            }
          }
        ) {
          Text(stringResource(id = android.R.string.ok), style = MaterialTheme.typography.bodyLarge)
        }
      },
      dismissButton = {
        TextButton(
          modifier = Modifier.padding(end = 16.dp),
          onClick = {
            textValueChanged = false
            validationMsg = null
            showDialog = false
          }
        ) {
          Text(
            stringResource(id = android.R.string.cancel),
            style = MaterialTheme.typography.bodyLarge
          )
        }
      },
      properties = DialogProperties(usePlatformDefaultWidth = false),
      containerColor = dialogBackgroundColor,
    )
  }
}

@Composable
private fun DialogHeader(dialogTitle: String?, dialogMessage: String?) {
  Column(modifier = Modifier.padding(16.dp)) {
    if (dialogTitle != null) {
      Text(
        text = dialogTitle,
        style = MaterialTheme.typography.titleLarge,
        textAlign = TextAlign.Center
      )
    }
    if (dialogMessage != null) {
      Spacer(modifier = Modifier.height(16.dp))
      Text(text = dialogMessage, style = MaterialTheme.typography.bodyMedium)
    }
  }
}
