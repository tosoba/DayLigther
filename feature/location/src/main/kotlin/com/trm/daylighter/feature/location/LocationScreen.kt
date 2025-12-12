package com.trm.daylighter.feature.location

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.windowsizeclass.WindowHeightSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.trm.daylighter.core.common.util.ext.CheckLocationSettingsResult
import com.trm.daylighter.core.common.util.ext.checkLocationSettings
import com.trm.daylighter.core.common.util.ext.checkPermissions
import com.trm.daylighter.core.common.util.ext.isValidEmail
import com.trm.daylighter.core.ui.composable.AlertDialogHeader
import com.trm.daylighter.core.ui.composable.EditTextPrefAlertDialog
import com.trm.daylighter.core.ui.composable.appBarTextStyle
import com.trm.daylighter.core.ui.composable.rememberMapViewWithLifecycle
import com.trm.daylighter.core.ui.local.LocalHeightSizeClass
import com.trm.daylighter.core.ui.local.LocalWidthSizeClass
import com.trm.daylighter.core.ui.theme.surfaceToTransparentVerticalGradient
import com.trm.daylighter.core.ui.util.rememberKeyboardOpen
import com.trm.daylighter.feature.location.model.LocationNameError
import com.trm.daylighter.feature.location.model.LocationPreparedToSave
import com.trm.daylighter.feature.location.model.LocationScreenMode
import com.trm.daylighter.feature.location.model.MapPosition
import com.trm.daylighter.feature.location.model.PermissionRequestMode
import com.trm.daylighter.feature.location.model.UserLocationRequestState
import com.trm.daylighter.feature.location.model.rememberSaveLocationState
import com.trm.daylighter.feature.location.model.rememberUserLocationRequestState
import com.trm.daylighter.feature.location.util.restorePosition
import com.trm.daylighter.feature.location.util.setDefaultConfig
import org.osmdroid.views.MapView
import timber.log.Timber
import com.trm.daylighter.core.common.R as commonR

const val locationRoute = "location_route"
const val locationIdParam = "location_id"
const val editLocationRoute = "$locationRoute/{$locationIdParam}"

@Composable
fun LocationRoute(
  onBackClick: () -> Unit,
  modifier: Modifier = Modifier,
  viewModel: LocationViewModel = hiltViewModel(),
) {
  LaunchedEffect(Unit) { viewModel.locationSavedFlow.collect { onBackClick() } }

  val context = LocalContext.current
  var showGeocodingEmailDialog by rememberSaveable { mutableStateOf(false) }
  val geocodingEmail = viewModel.geocodingEmailFlow.collectAsStateWithLifecycle(initialValue = "")

  val keyboardOpen = rememberKeyboardOpen()

  EditTextPrefAlertDialog(
    isShowing = showGeocodingEmailDialog,
    hide = { showGeocodingEmailDialog = false },
    prefValue = geocodingEmail.value.orEmpty(),
    editPref = viewModel::setGeocodingEmail,
    title = {
      AnimatedVisibility(
        visible =
          !keyboardOpen.value || LocalHeightSizeClass.current != WindowHeightSizeClass.Compact
      ) {
        AlertDialogHeader(
          modifier = Modifier.padding(8.dp),
          dialogTitle = stringResource(commonR.string.geocoding_email_pref_dialog_title),
          dialogMessage = stringResource(commonR.string.geocoding_email_pref_dialog_message),
        )
      }
    },
    editTextPlaceholder = stringResource(commonR.string.geocoding_email_value_placeholder),
    validateValue = { value -> value.isValidEmail()?.let(context::getString) },
  )

  val mapPosition = viewModel.mapPositionFlow.collectAsStateWithLifecycle()
  val locationPreparedToSave =
    viewModel.locationPreparedToSaveFlow.collectAsStateWithLifecycle(initialValue = null)
  val isLoading = viewModel.loadingFlow.collectAsStateWithLifecycle(initialValue = false)
  val userLocationNotFound =
    viewModel.userLocationNotFoundFlow.collectAsStateWithLifecycle(initialValue = false)
  val locationName = viewModel.locationNameReadyFlow.collectAsStateWithLifecycle(initialValue = "")
  val isLocationNameLoading =
    viewModel.locationNameLoadingFlow.collectAsStateWithLifecycle(initialValue = false)
  val locationNameFailureMessage =
    viewModel.locationNameFailureMessageFlow.collectAsStateWithLifecycle(initialValue = null)
  val isGeocodeEmailPreferenceSet =
    viewModel.isGeocodeEmailPreferenceSetFlow.collectAsStateWithLifecycle(initialValue = false)

  LocationScreen(
    screenMode = viewModel.screenMode,
    mapPosition = mapPosition.value,
    onMapViewPause = viewModel::onMapViewPause,
    locationPreparedToSave = locationPreparedToSave.value,
    isLoading = isLoading.value,
    userLocationNotFound = userLocationNotFound.value,
    saveSpecifiedLocationClick = viewModel::requestSaveSpecifiedLocation,
    requestGetAndSaveUserLocation = viewModel::requestGetAndSaveUserLocation,
    cancelCurrentSaveLocation = viewModel::cancelCurrentSaveLocationRequest,
    onSaveLocationClick = viewModel::saveLocation,
    locationName = locationName.value,
    isLocationNameLoading = isLocationNameLoading.value,
    locationNameFailureMessage = locationNameFailureMessage.value,
    onLocationNameChange = viewModel::inputLocationName,
    clearLocationName = viewModel::clearLocationName,
    geocodeButtonText =
      stringResource(
        id = if (isGeocodeEmailPreferenceSet.value) R.string.geocode else R.string.enable_geocoding
      ),
    onGeocodeClick =
      if (isGeocodeEmailPreferenceSet.value) viewModel::getLocationDisplayName
      else { _, _ -> showGeocodingEmailDialog = true },
    onBackClick = onBackClick,
    modifier = modifier,
  )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LocationScreen(
  screenMode: LocationScreenMode,
  mapPosition: MapPosition,
  onMapViewPause: (MapPosition) -> Unit,
  locationPreparedToSave: LocationPreparedToSave?,
  isLoading: Boolean,
  userLocationNotFound: Boolean,
  saveSpecifiedLocationClick: (lat: Double, lng: Double) -> Unit,
  requestGetAndSaveUserLocation: () -> Unit,
  cancelCurrentSaveLocation: () -> Unit,
  onSaveLocationClick: (lat: Double, lng: Double, name: String) -> Unit,
  locationName: String,
  isLocationNameLoading: Boolean,
  @StringRes locationNameFailureMessage: Int?,
  onLocationNameChange: (String) -> Unit,
  clearLocationName: () -> Unit,
  geocodeButtonText: String,
  onGeocodeClick: (lat: Double, lng: Double) -> Unit,
  onBackClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val context = LocalContext.current
  val userLocationRequestState = rememberUserLocationRequestState()
  val locationPermissionsRequestLauncher =
    rememberLocationPermissionsResultLauncher(userLocationRequestState = userLocationRequestState)

  fun Context.checkLocationPermissions() {
    checkPermissions(
      permissions = userLocationRequestState.locationPermissions,
      onNotGranted = {
        when (userLocationRequestState.permissionRequestMode) {
          PermissionRequestMode.PERMISSION_REQUEST_DIALOG -> {
            locationPermissionsRequestLauncher.launch(userLocationRequestState.locationPermissions)
          }
          PermissionRequestMode.APP_DETAILS_SETTINGS -> {
            context.startActivity(
              Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                .setData(Uri.fromParts("package", context.packageName, null))
            )
          }
        }
      },
      onGranted = { userLocationRequestState.shouldCheckIfLocationEnabled = true },
    )
  }

  CheckLocationSettingsEffect(
    userLocationRequestState = userLocationRequestState,
    onLocationEnabled = requestGetAndSaveUserLocation,
  )

  UserLocationNotFoundToastEffect(userLocationNotFound = userLocationNotFound)

  val saveLocationState =
    rememberSaveLocationState(
      latitude = locationPreparedToSave?.latitude ?: mapPosition.latitude,
      longitude = locationPreparedToSave?.longitude ?: mapPosition.longitude,
    )
  var locationNameError by rememberSaveable { mutableStateOf(LocationNameError.NO_ERROR) }

  var sheetVisible by rememberSaveable { mutableStateOf(false) }

  LaunchedEffect(locationPreparedToSave) {
    if (locationPreparedToSave?.isUser == true) sheetVisible = true
  }

  var isInfoDialogShown by rememberSaveable { mutableStateOf(false) }

  LaunchedEffect(sheetVisible) {
    if (!sheetVisible) {
      clearLocationName()
      cancelCurrentSaveLocation()
    }
  }

  LocationScaffold(
    mapView =
      rememberMapViewWithLifecycle(
        onPause = {
          onMapViewPause(
            MapPosition(
              latitude = it.mapCenter.latitude,
              longitude = it.mapCenter.longitude,
              zoom = it.zoomLevelDouble,
              orientation = it.mapOrientation,
              label = mapPosition.label,
            )
          )
        }
      ),
    mapPosition = mapPosition,
    isLoading = isLoading,
    isInfoDialogShown = isInfoDialogShown,
    onInfoClick = { isInfoDialogShown = true },
    onInfoDialogDismissRequest = { isInfoDialogShown = false },
    saveSpecifiedLocation = { latitude, longitude ->
      saveSpecifiedLocationClick(latitude, longitude)
      sheetVisible = true
    },
    onUserLocationClick = context::checkLocationPermissions,
    cancelCurrentSaveLocation = cancelCurrentSaveLocation,
    onBackClick = onBackClick,
    modalSheet = {
      if (sheetVisible) {
        ModalBottomSheet(onDismissRequest = { sheetVisible = false }) {
          ModalSheetContent(
            headerLabel = stringResource(R.string.enter_location_name),
            nameValue = locationName,
            isNameLoading = isLocationNameLoading,
            nameFailureMessage = locationNameFailureMessage,
            onNameValueChange = {
              locationNameError = LocationNameError.NO_ERROR
              onLocationNameChange(it)
            },
            nameError = locationNameError,
            geocodeButtonText = geocodeButtonText,
            onGeocodeClick = {
              onGeocodeClick(saveLocationState.latitude, saveLocationState.longitude)
            },
            onSaveClick = {
              if (locationName.isBlank()) {
                locationNameError = LocationNameError.BLANK
              } else {
                locationNameError = LocationNameError.NO_ERROR
                sheetVisible = false
                onSaveLocationClick(
                  saveLocationState.latitude,
                  saveLocationState.longitude,
                  locationName,
                )
              }
            },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
          )
        }
      }
    },
    locationPermissionDialog = {
      LocationPermissionInfoDialog(
        dialogVisible = userLocationRequestState.permissionInfoDialogVisible,
        permissionRequestMode = userLocationRequestState.permissionRequestMode,
        onOkClick = {
          userLocationRequestState.permissionInfoDialogVisible = false
          context.checkLocationPermissions()
        },
        onDismiss = { userLocationRequestState.permissionInfoDialogVisible = false },
        modifier = Modifier.align(Alignment.Center).wrapContentHeight(),
      )
    },
    modifier = modifier,
  )
}

@Composable
private fun LocationScaffold(
  mapView: MapView,
  mapPosition: MapPosition,
  isLoading: Boolean,
  isInfoDialogShown: Boolean,
  onInfoClick: () -> Unit,
  onInfoDialogDismissRequest: () -> Unit,
  saveSpecifiedLocation: (lat: Double, lng: Double) -> Unit,
  onUserLocationClick: () -> Unit,
  cancelCurrentSaveLocation: () -> Unit,
  onBackClick: () -> Unit,
  modalSheet: @Composable () -> Unit,
  locationPermissionDialog: @Composable BoxScope.() -> Unit,
  modifier: Modifier = Modifier,
) {
  Scaffold(modifier = modifier, contentWindowInsets = WindowInsets(0, 0, 0, 0)) { padding ->
    Box(modifier = Modifier.fillMaxSize().padding(padding)) {
      MapView(mapView, mapPosition, modifier = Modifier.fillMaxSize())
      MarkerIcon(modifier = Modifier.align(Alignment.Center))

      Column(
        modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp),
        horizontalAlignment = Alignment.End,
      ) {
        UserLocationButton(visible = !isLoading, onUserLocationClick = onUserLocationClick)

        Spacer(modifier = Modifier.height(16.dp))

        SaveSpecifiedLocationButton(
          imageVector = if (!isLoading) Icons.Filled.Done else Icons.Filled.Cancel
        ) {
          if (!isLoading) {
            val mapCenter = mapView.mapCenter
            saveSpecifiedLocation(mapCenter.latitude, mapCenter.longitude)
          } else {
            cancelCurrentSaveLocation()
          }
        }
      }

      LocationAppBar(
        mapPosition = mapPosition,
        onBackClick = onBackClick,
        onInfoClick = onInfoClick,
      )

      if (isInfoDialogShown) {
        LocationInfoDialog(onDismissRequest = onInfoDialogDismissRequest)
      }

      if (isLoading) {
        LinearProgressIndicator(modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth())
      }

      locationPermissionDialog()
    }

    modalSheet()
  }
}

@Composable
private fun LocationInfoDialog(onDismissRequest: () -> Unit) {
  AlertDialog(
    onDismissRequest = onDismissRequest,
    title = { Text(text = stringResource(R.string.location_info_dialog_title)) },
    text = { Text(text = stringResource(R.string.location_info_dialog_text)) },
    confirmButton = {
      TextButton(onClick = onDismissRequest) {
        Text(text = stringResource(android.R.string.ok), style = MaterialTheme.typography.bodyLarge)
      }
    },
  )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LocationAppBar(
  mapPosition: MapPosition,
  onBackClick: () -> Unit,
  onInfoClick: () -> Unit,
) {
  CenterAlignedTopAppBar(
    modifier = Modifier.background(surfaceToTransparentVerticalGradient),
    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
    title = {
      Text(
        text =
          mapPosition.label.takeIf(String::isNotEmpty)
            ?: stringResource(commonR.string.new_location),
        style = appBarTextStyle(),
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        textAlign = TextAlign.Center,
        modifier = Modifier.basicMarquee(iterations = Int.MAX_VALUE).padding(horizontal = 12.dp),
      )
    },
    navigationIcon = {
      SmallFloatingActionButton(
        onClick = onBackClick,
        modifier = Modifier.padding(start = 4.dp, top = 4.dp),
      ) {
        Icon(
          imageVector = Icons.AutoMirrored.Filled.ArrowBack,
          contentDescription = stringResource(commonR.string.back),
          tint = MaterialTheme.colorScheme.onSurface,
        )
      }
    },
    actions = {
      SmallFloatingActionButton(
        onClick = onInfoClick,
        modifier = Modifier.padding(end = 4.dp, top = 4.dp),
      ) {
        Icon(
          imageVector = Icons.Filled.Info,
          contentDescription = stringResource(R.string.location_info_dialog_text),
          tint = MaterialTheme.colorScheme.onSurface,
        )
      }
    },
  )
}

@Composable
private fun SaveSpecifiedLocationButton(imageVector: ImageVector, onClick: () -> Unit) {
  ExtendedFloatingActionButton(
    text = { Text(stringResource(commonR.string.confirm)) },
    icon = {
      Icon(imageVector = imageVector, contentDescription = stringResource(R.string.save_location))
    },
    onClick = onClick,
  )
}

@Composable
private fun ColumnScope.UserLocationButton(visible: Boolean, onUserLocationClick: () -> Unit) {
  AnimatedVisibility(visible = visible, enter = fadeIn(), exit = fadeOut()) {
    FloatingActionButton(onClick = onUserLocationClick) {
      Icon(
        imageVector = Icons.Filled.MyLocation,
        contentDescription = stringResource(R.string.my_location),
      )
    }
  }
}

@Composable
private fun LoadingProgressIndicator(visible: Boolean, modifier: Modifier = Modifier) {
  AnimatedVisibility(visible = visible, enter = fadeIn(), exit = fadeOut(), modifier = modifier) {
    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
  }
}

@Composable
private fun MapView(mapView: MapView, mapPosition: MapPosition, modifier: Modifier = Modifier) {
  val darkMode = isSystemInDarkTheme()
  AndroidView(
    factory = { mapView },
    update = {
      it.setDefaultConfig(darkMode = darkMode)
      it.restorePosition(position = mapPosition)
    },
    modifier = modifier,
  )
}

@Composable
private fun MarkerIcon(modifier: Modifier = Modifier) {
  Icon(
    painter = painterResource(commonR.drawable.marker),
    contentDescription = stringResource(commonR.string.location_marker),
    modifier = modifier,
  )
}

@Composable
private fun ModalSheetContent(
  headerLabel: String,
  nameValue: String,
  isNameLoading: Boolean,
  @StringRes nameFailureMessage: Int?,
  onNameValueChange: (String) -> Unit,
  nameError: LocationNameError,
  geocodeButtonText: String,
  onSaveClick: () -> Unit,
  onGeocodeClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Top,
    modifier = modifier,
  ) {
    Text(
      text = headerLabel,
      style = MaterialTheme.typography.headlineMedium,
      maxLines = 2,
      modifier = Modifier.padding(12.dp).fillMaxWidth(),
    )

    OutlinedTextField(
      value = nameValue,
      onValueChange = onNameValueChange,
      label = { Text(text = stringResource(R.string.name)) },
      singleLine = true,
      isError = nameError != LocationNameError.NO_ERROR,
      trailingIcon = {
        AnimatedVisibility(visible = nameValue.isNotEmpty(), enter = fadeIn(), exit = fadeOut()) {
          Icon(
            imageVector = Icons.Filled.Clear,
            contentDescription = stringResource(commonR.string.clear),
            modifier = Modifier.clickable { onNameValueChange("") },
          )
        }
      },
      modifier = Modifier.padding(12.dp).fillMaxWidth(),
    )

    LoadingProgressIndicator(
      visible = isNameLoading,
      modifier = Modifier.padding(horizontal = 12.dp).fillMaxWidth(),
    )

    AnimatedVisibility(
      visible = nameError != LocationNameError.NO_ERROR,
      enter = fadeIn(),
      exit = fadeOut(),
    ) {
      Text(
        modifier = Modifier.padding(horizontal = 12.dp),
        text =
          when (nameError) {
            LocationNameError.BLANK -> stringResource(R.string.location_name_cannot_be_blank)
            LocationNameError.NO_ERROR -> ""
          },
        fontSize = 14.sp,
        color = MaterialTheme.colorScheme.error,
      )
    }

    if (LocalWidthSizeClass.current == WindowWidthSizeClass.Compact) {
      ModalSheetButtons(
        geocodeButtonText = geocodeButtonText,
        onGeocodeClick = onGeocodeClick,
        onSaveClick = onSaveClick,
      )
    } else {
      ModalSheetButtonsRow(
        geocodeButtonText = geocodeButtonText,
        onGeocodeClick = onGeocodeClick,
        onSaveClick = onSaveClick,
      )
    }

    Spacer(modifier = Modifier.height(12.dp))
  }

  ToastMessageEffect(message = nameFailureMessage)
}

@Composable
private fun ModalSheetButtonsRow(
  geocodeButtonText: String,
  onGeocodeClick: () -> Unit,
  onSaveClick: () -> Unit,
) {
  Row(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
    OutlinedButton(
      onClick = onGeocodeClick,
      modifier = Modifier.height(IntrinsicSize.Max).weight(.5f),
    ) {
      Text(
        text = geocodeButtonText,
        maxLines = 1,
        modifier = Modifier.basicMarquee(iterations = Int.MAX_VALUE),
      )
    }

    Spacer(modifier = Modifier.width(8.dp))

    Button(onClick = onSaveClick, modifier = Modifier.height(IntrinsicSize.Max).weight(.5f)) {
      Text(
        text = stringResource(R.string.save),
        maxLines = 1,
        modifier = Modifier.basicMarquee(iterations = Int.MAX_VALUE),
      )
    }
  }
}

@Composable
private fun ModalSheetButtons(
  geocodeButtonText: String,
  onGeocodeClick: () -> Unit,
  onSaveClick: () -> Unit,
) {
  OutlinedButton(
    onClick = onGeocodeClick,
    modifier = Modifier.fillMaxWidth().padding(top = 12.dp, start = 12.dp, end = 12.dp),
  ) {
    Text(
      text = geocodeButtonText,
      maxLines = 1,
      modifier = Modifier.basicMarquee(iterations = Int.MAX_VALUE),
    )
  }

  Spacer(modifier = Modifier.height(8.dp))

  Button(
    onClick = onSaveClick,
    modifier = Modifier.fillMaxWidth().padding(start = 12.dp, end = 12.dp, bottom = 12.dp),
  ) {
    Text(
      text = stringResource(R.string.save),
      maxLines = 1,
      modifier = Modifier.basicMarquee(iterations = Int.MAX_VALUE),
    )
  }
}

@Composable
private fun ToastMessageEffect(@StringRes message: Int?) {
  val context = LocalContext.current
  var toast: Toast? by remember { mutableStateOf(null) }

  LaunchedEffect(message) {
    toast =
      if (message != null) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).apply { show() }
      } else {
        toast?.cancel()
        null
      }
  }
}

@Composable
private fun rememberLocationPermissionsResultLauncher(
  userLocationRequestState: UserLocationRequestState
): ManagedActivityResultLauncher<Array<String>, Map<String, Boolean>> {
  val activity = requireNotNull(LocalActivity.current)
  return rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
    permissionsMap ->
    val allGranted = permissionsMap.values.all { it }
    if (allGranted) {
      Timber.tag("USER_LOCATION_PERMISSIONS").d("Location permissions are granted.")
      userLocationRequestState.shouldCheckIfLocationEnabled = true
    } else {
      val shouldShowRationale =
        userLocationRequestState.locationPermissions.any { permission ->
          ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)
        }
      userLocationRequestState.permissionRequestMode =
        if (shouldShowRationale) PermissionRequestMode.PERMISSION_REQUEST_DIALOG
        else PermissionRequestMode.APP_DETAILS_SETTINGS
      userLocationRequestState.permissionInfoDialogVisible = true
    }
  }
}

@Composable
private fun CheckLocationSettingsEffect(
  userLocationRequestState: UserLocationRequestState,
  onLocationEnabled: () -> Unit,
) {
  val context = LocalContext.current
  val locationSettingsRequestLauncher =
    rememberLocationSettingsActivityResultLauncher(onLocationEnabled = onLocationEnabled)

  LaunchedEffect(userLocationRequestState.shouldCheckIfLocationEnabled) {
    if (!userLocationRequestState.shouldCheckIfLocationEnabled) return@LaunchedEffect

    when (val result = context.checkLocationSettings()) {
      is CheckLocationSettingsResult.Enabled -> {
        Timber.tag("USER_LOCATION_CHECK").d("Location is enabled after check.")
        onLocationEnabled()
      }
      is CheckLocationSettingsResult.DisabledResolvable -> {
        locationSettingsRequestLauncher.launch(result.intentSenderRequest)
      }
      is CheckLocationSettingsResult.DisabledNonResolvable -> {
        Toast.makeText(
            context,
            R.string.location_disabled_non_resolvable_message,
            Toast.LENGTH_LONG,
          )
          .show()
        Timber.tag("USER_LOCATION_CHECK").d("Location is disabled after check - NON resolvable.")
      }
    }

    userLocationRequestState.shouldCheckIfLocationEnabled = false
  }
}

@Composable
private fun rememberLocationSettingsActivityResultLauncher(
  onLocationEnabled: () -> Unit
): ManagedActivityResultLauncher<IntentSenderRequest, ActivityResult> =
  rememberLauncherForActivityResult(
    contract = ActivityResultContracts.StartIntentSenderForResult()
  ) { activityResult ->
    if (activityResult.resultCode == Activity.RESULT_OK) {
      Timber.tag("USER_LOCATION_DIALOG")
        .d("Location was enabled after showing location settings dialog.")
      onLocationEnabled()
    } else {
      Timber.tag("USER_LOCATION_DIALOG")
        .d("Location was NOT enabled after showing location settings dialog.")
    }
  }

@Composable
private fun UserLocationNotFoundToastEffect(userLocationNotFound: Boolean) {
  val context = LocalContext.current
  var toast: Toast? by remember { mutableStateOf(null) }

  LaunchedEffect(userLocationNotFound) {
    toast =
      if (userLocationNotFound) {
        Toast.makeText(context, R.string.location_not_found, Toast.LENGTH_LONG).apply { show() }
      } else {
        toast?.cancel()
        null
      }
  }
}

@Composable
private fun LocationPermissionInfoDialog(
  dialogVisible: Boolean,
  permissionRequestMode: PermissionRequestMode,
  onOkClick: () -> Unit,
  onDismiss: () -> Unit,
  modifier: Modifier = Modifier,
) {
  AnimatedVisibility(visible = dialogVisible, enter = fadeIn(), exit = fadeOut()) {
    AlertDialog(
      modifier = modifier,
      onDismissRequest = onDismiss,
      confirmButton = {
        TextButton(onClick = onOkClick) { Text(text = stringResource(android.R.string.ok)) }
      },
      dismissButton = {
        TextButton(onClick = onDismiss) { Text(text = stringResource(android.R.string.cancel)) }
      },
      title = {
        Text(
          text = stringResource(R.string.location_permissions_dialog_title),
          textAlign = TextAlign.Center,
        )
      },
      text = {
        Text(
          text =
            when (permissionRequestMode) {
              PermissionRequestMode.PERMISSION_REQUEST_DIALOG -> {
                stringResource(R.string.location_permissions_dialog_rationale_text)
              }
              PermissionRequestMode.APP_DETAILS_SETTINGS -> {
                stringResource(R.string.location_permissions_dialog_settings_text)
              }
            }
        )
      },
    )
  }
}
