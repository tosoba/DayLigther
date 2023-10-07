package com.trm.daylighter.feature.settings

import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jamal.composeprefs3.ui.GroupHeader
import com.jamal.composeprefs3.ui.LocalPrefsDataStore
import com.jamal.composeprefs3.ui.PrefsScope
import com.jamal.composeprefs3.ui.PrefsScreen
import com.jamal.composeprefs3.ui.prefs.*
import com.trm.daylighter.core.common.R as commonR
import com.trm.daylighter.core.common.util.ext.isValidEmail
import com.trm.daylighter.core.datastore.PreferencesDataStoreKeys
import com.trm.daylighter.core.datastore.preferencesDataStore
import com.trm.daylighter.core.ui.composable.DayLighterTopAppBar
import com.trm.daylighter.core.ui.composable.DrawerMenuIconButton
import com.trm.daylighter.core.ui.composable.EditTextPrefAlertDialog
import com.trm.daylighter.core.ui.util.usingPermanentNavigationDrawer
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import timber.log.Timber

const val settingsRoute = "settings_route"

@Composable
fun SettingsRoute(
  modifier: Modifier = Modifier,
  onDrawerMenuClick: () -> Unit,
  viewModel: SettingsViewModel = hiltViewModel()
) {
  val context = LocalContext.current
  val isGeocodeEmailPreferenceSet =
    viewModel.isGeocodeEmailPreferenceSetFlow.collectAsStateWithLifecycle(initialValue = false)
  val geocodingDisabledMessage = stringResource(R.string.geocoding_is_disabled)
  val locationsDeletedMessage = stringResource(R.string.locations_deleted)

  SettingsScreen(
    isGeocodeEmailPreferenceSet = isGeocodeEmailPreferenceSet.value,
    onDrawerMenuClick = onDrawerMenuClick,
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsScreen(
  isGeocodeEmailPreferenceSet: Boolean,
  onDrawerMenuClick: () -> Unit,
  onDisableGeocodingClick: () -> Unit,
  onClearLocationsClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  Column(modifier = modifier) {
    DayLighterTopAppBar(
      title = stringResource(commonR.string.settings),
      navigationIcon = {
        if (!usingPermanentNavigationDrawer) {
          DrawerMenuIconButton(onClick = onDrawerMenuClick)
        }
      }
    )

    PrefsScreen(
      dataStore = LocalContext.current.preferencesDataStore,
      modifier = Modifier.fillMaxWidth().weight(1f)
    ) {
      prefsGroup({
        GroupHeader(
          title = stringResource(R.string.geocoding_pref_group_title),
          color = MaterialTheme.colorScheme.secondary
        )
      }) {
        editGeocodingEmailPreferenceItem()

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
}

@OptIn(ExperimentalComposeUiApi::class)
private fun PrefsScope.editGeocodingEmailPreferenceItem() {
  prefsItem {
    val context = LocalContext.current
    EditTextPref(
      key = PreferencesDataStoreKeys.GEOCODING_EMAIL,
      title = stringResource(R.string.geocoding_email_pref_title),
      summary = stringResource(R.string.geocoding_email_pref_summary),
      dialogTitle = stringResource(commonR.string.geocoding_email_pref_dialog_title),
      dialogMessage = stringResource(commonR.string.geocoding_email_pref_dialog_message),
      validateValue = { value -> value.isValidEmail()?.let(context::getString) },
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

  TextPref(
    title = title,
    modifier = modifier,
    summary = summary,
    textColor = textColor,
    enabled = enabled,
    onClick = { if (enabled) showDialog = !showDialog },
  )

  val datastore = LocalPrefsDataStore.current
  val prefKey = stringPreferencesKey(key)
  val prefValue by
    remember { datastore.data.map { preferences -> preferences[prefKey] ?: defaultValue } }
      .collectAsStateWithLifecycle(initialValue = defaultValue)

  val scope = rememberCoroutineScope()

  fun editPref(textValue: String) {
    scope.launch {
      try {
        datastore.edit { preferences -> preferences[prefKey] = textValue }
        onValueSaved(textValue)
      } catch (ex: Exception) {
        Timber.tag("EditTextPref").e(ex, "Could not write pref $key to database.")
      }
    }
  }

  EditTextPrefAlertDialog(
    isShowing = showDialog,
    hide = { showDialog = false },
    prefValue = prefValue,
    editPref = ::editPref,
    dialogTitle = dialogTitle,
    dialogMessage = dialogMessage,
    editTextPlaceholder = stringResource(commonR.string.geocoding_email_value_placeholder),
    onValueChange = onValueChange,
    validateValue = validateValue,
    dialogBackgroundColor = dialogBackgroundColor
  )
}
