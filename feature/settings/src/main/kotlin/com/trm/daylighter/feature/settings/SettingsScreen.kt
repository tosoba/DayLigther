package com.trm.daylighter.feature.settings

import android.util.Patterns
import android.widget.Toast
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.jamal.composeprefs3.ui.GroupHeader
import com.jamal.composeprefs3.ui.LocalPrefsDataStore
import com.jamal.composeprefs3.ui.PrefsScope
import com.jamal.composeprefs3.ui.PrefsScreen
import com.jamal.composeprefs3.ui.prefs.*
import com.trm.daylighter.core.datastore.PreferencesDataStoreKeys
import com.trm.daylighter.core.datastore.preferencesDataStore
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import timber.log.Timber

private const val settingsAutoShowEmailDialogParam = "auto_show_email_dialog"
const val settingsRoute = "settings_route"
const val settingsAutoShowEmailDialogRoute =
  "$settingsRoute?$settingsAutoShowEmailDialogParam={$settingsAutoShowEmailDialogParam}"

fun settingsNavigationRoute(autoShowEmailDialog: Boolean) =
  "$settingsRoute?$settingsAutoShowEmailDialogParam=$autoShowEmailDialog"

fun NavGraphBuilder.settingsComposable(modifier: Modifier = Modifier) {
  composable(
    route = settingsAutoShowEmailDialogRoute,
    arguments =
      listOf(
        navArgument(settingsAutoShowEmailDialogParam) {
          type = NavType.StringType
          nullable = true
        }
      )
  ) {
    SettingsRoute(
      autoShowEmailDialog = it.arguments?.getString(settingsAutoShowEmailDialogParam)?.toBoolean()
          ?: false,
      modifier = modifier
    )
  }
}

@Composable
private fun SettingsRoute(
  modifier: Modifier = Modifier,
  autoShowEmailDialog: Boolean = false,
  viewModel: SettingsViewModel = hiltViewModel()
) {
  val context = LocalContext.current
  val isGeocodeEmailPreferenceSet =
    viewModel.isGeocodeEmailPreferenceSetFlow.collectAsStateWithLifecycle(initialValue = false)
  val geocodingDisabledMessage = stringResource(R.string.geocoding_is_disabled)
  val locationsDeletedMessage = stringResource(R.string.locations_deleted)

  SettingsScreen(
    isGeocodeEmailPreferenceSet = isGeocodeEmailPreferenceSet.value,
    autoShowEmailDialog = autoShowEmailDialog,
    onDisableGeocodingClick = {
      viewModel.clearGeocodingEmail()
      Toast.makeText(context, geocodingDisabledMessage, Toast.LENGTH_SHORT).show()
    },
    onClearLocationsClick = {
      viewModel.deleteLocations()
      Toast.makeText(context, locationsDeletedMessage, Toast.LENGTH_SHORT).show()
    },
    modifier = modifier
  )
}

@Composable
private fun SettingsScreen(
  isGeocodeEmailPreferenceSet: Boolean,
  autoShowEmailDialog: Boolean,
  onDisableGeocodingClick: () -> Unit,
  onClearLocationsClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  PrefsScreen(dataStore = LocalContext.current.preferencesDataStore, modifier = modifier) {
    prefsGroup({
      GroupHeader(
        title = stringResource(R.string.geocoding_pref_group_title),
        color = MaterialTheme.colorScheme.secondary
      )
    }) {
      editGeocodingEmailPreferenceItem(autoShowEmailDialog = autoShowEmailDialog)

      if (isGeocodeEmailPreferenceSet) {
        disableGeocodingPreferenceItem(onClick = onDisableGeocodingClick)
      }
    }

    prefsGroup({
      GroupHeader(
        title = stringResource(R.string.locations_pref_group_title),
        color = MaterialTheme.colorScheme.secondary
      )
    }) {
      clearLocationsPreferenceItem(onClearLocationsClick = onClearLocationsClick)
    }
  }
}

@OptIn(ExperimentalComposeUiApi::class)
private fun PrefsScope.editGeocodingEmailPreferenceItem(autoShowEmailDialog: Boolean) {
  prefsItem {
    val emailEmptyError = stringResource(R.string.email_empty_error)
    val invalidEmailAddress = stringResource(R.string.invalid_email_error)
    EditTextPref(
      key = PreferencesDataStoreKeys.GEOCODING_EMAIL,
      title = stringResource(R.string.geocoding_email_pref_title),
      summary = stringResource(R.string.geocoding_email_pref_summary),
      autoShowDialog = autoShowEmailDialog,
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

private fun PrefsScope.clearLocationsPreferenceItem(onClearLocationsClick: () -> Unit) {
  prefsItem {
    var showDialog by rememberSaveable { mutableStateOf(false) }

    TextPref(
      title = stringResource(R.string.clear_locations_data_pref_title),
      summary = stringResource(R.string.clear_locations_data_summary),
      enabled = true,
      onClick = { showDialog = true }
    )

    if (showDialog) {
      AlertDialog(
        onDismissRequest = { showDialog = false },
        title = {
          DialogHeader(
            dialogTitle = stringResource(R.string.clear_locations_data_pref_title),
            dialogMessage = stringResource(R.string.clear_locations_data_pref_dialog_message)
          )
        },
        confirmButton = {
          TextButton(
            modifier = Modifier.padding(end = 16.dp),
            onClick = {
              onClearLocationsClick()
              showDialog = false
            }
          ) {
            Text(
              text = stringResource(android.R.string.ok),
              style = MaterialTheme.typography.bodyLarge
            )
          }
        },
        dismissButton = {
          TextButton(modifier = Modifier.padding(end = 16.dp), onClick = { showDialog = false }) {
            Text(
              text = stringResource(android.R.string.cancel),
              style = MaterialTheme.typography.bodyLarge
            )
          }
        },
      )
    }
  }
}

private fun PrefsScope.disableGeocodingPreferenceItem(onClick: () -> Unit) {
  prefsItem {
    TextPref(
      title = stringResource(R.string.disable_geocoding_email_pref_title),
      summary = stringResource(R.string.disable_geocoding_email_pref_summary),
      enabled = true,
      onClick = onClick
    )
  }
}

@ExperimentalComposeUiApi
@Composable
private fun EditTextPref(
  key: String,
  title: String,
  modifier: Modifier = Modifier,
  summary: String? = null,
  autoShowDialog: Boolean = false,
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
  val scope = rememberCoroutineScope()

  val datastore = LocalPrefsDataStore.current
  val prefKey = stringPreferencesKey(key)
  val prefValue by
    remember { datastore.data.map { preferences -> preferences[prefKey] ?: defaultValue } }
      .collectAsStateWithLifecycle(initialValue = defaultValue)

  var textValue by rememberSaveable(prefValue) { mutableStateOf(prefValue) }
  var textValueChanged by rememberSaveable { mutableStateOf(false) }
  var validationMsg: String? by rememberSaveable { mutableStateOf(null) }

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

  var showDialog by rememberSaveable { mutableStateOf(autoShowDialog) }

  TextPref(
    title = title,
    modifier = modifier,
    summary = summary,
    textColor = textColor,
    enabled = enabled,
    onClick = { if (enabled) showDialog = !showDialog },
  )

  if (showDialog) {
    LaunchedEffect(Unit) { if (!textValueChanged) textValue = prefValue }

    AlertDialog(
      modifier = Modifier.fillMaxWidth(0.9f),
      onDismissRequest = {
        textValueChanged = false
        textValue = prefValue
        showDialog = false
      },
      title = { DialogHeader(dialogTitle = dialogTitle, dialogMessage = dialogMessage) },
      text = {
        OutlinedTextField(
          value = textValue,
          modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
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
          Text(
            text = stringResource(android.R.string.ok),
            style = MaterialTheme.typography.bodyLarge
          )
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
            text = stringResource(android.R.string.cancel),
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
