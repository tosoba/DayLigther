package com.trm.daylighter.feature.location

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.WindowHeightSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.trm.daylighter.core.common.R as commonR
import com.trm.daylighter.core.common.util.ext.*
import com.trm.daylighter.core.ui.composable.AlertDialogHeader
import com.trm.daylighter.core.ui.composable.EditTextPrefAlertDialog
import com.trm.daylighter.core.ui.composable.appBarTextStyle
import com.trm.daylighter.core.ui.composable.rememberMapViewWithLifecycle
import com.trm.daylighter.core.ui.local.LocalHeightSizeClass
import com.trm.daylighter.core.ui.local.LocalWidthSizeClass
import com.trm.daylighter.core.ui.theme.surfaceToTransparentVerticalGradient
import com.trm.daylighter.core.ui.util.rememberKeyboardOpen
import com.trm.daylighter.feature.location.model.*
import com.trm.daylighter.feature.location.util.restorePosition
import com.trm.daylighter.feature.location.util.setDefaultConfig
import org.osmdroid.views.MapView
import timber.log.Timber

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

  val mapView =
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
    )

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
  val sheetHeaderLabel =
    stringResource(
      id =
        when (screenMode) {
          LocationScreenMode.ADD -> commonR.string.new_location
          LocationScreenMode.EDIT -> R.string.edit_location
        }
    )

  LaunchedEffect(locationPreparedToSave) {
    if (locationPreparedToSave?.isUser == true) sheetVisible = true
  }

  @Composable
  fun ModalSheetContent(modifier: Modifier = Modifier) {
    ModalSheetContent(
      headerLabel = sheetHeaderLabel,
      nameValue = locationName,
      isNameLoading = isLocationNameLoading,
      nameFailureMessage = locationNameFailureMessage,
      onNameValueChange = {
        locationNameError = LocationNameError.NO_ERROR
        onLocationNameChange(it)
      },
      nameError = locationNameError,
      geocodeButtonText = geocodeButtonText,
      onGeocodeClick = { onGeocodeClick(saveLocationState.latitude, saveLocationState.longitude) },
      onSaveClick = {
        if (locationName.isBlank()) {
          locationNameError = LocationNameError.BLANK
        } else {
          locationNameError = LocationNameError.NO_ERROR
          sheetVisible = false
          onSaveLocationClick(saveLocationState.latitude, saveLocationState.longitude, locationName)
        }
      },
      modifier = modifier,
    )
  }

  var isInfoDialogShown by rememberSaveable { mutableStateOf(false) }

  @Composable
  fun LocationScaffold() {
    LocationScaffold(
      mapView = mapView,
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
        if (sheetVisible && LocalHeightSizeClass.current != WindowHeightSizeClass.Compact) {
          ModalBottomSheet(onDismissRequest = { sheetVisible = false }) {
            ModalSheetContent(modifier = Modifier.padding(horizontal = 20.dp).fillMaxWidth())
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

  LaunchedEffect(sheetVisible) {
    if (!sheetVisible) {
      clearLocationName()
      cancelCurrentSaveLocation()
    }
  }

  if (LocalHeightSizeClass.current != WindowHeightSizeClass.Compact) {
    LocationScaffold()
  } else {
    val drawerState = remember {
      DrawerState(initialValue = if (sheetVisible) DrawerValue.Open else DrawerValue.Closed)
    }
    LaunchedEffect(sheetVisible) { drawerState.run { if (sheetVisible) open() else close() } }
    LaunchedEffect(drawerState.currentValue) {
      if (drawerState.isClosed && !drawerState.isAnimationRunning) sheetVisible = false
    }
    BackHandler(enabled = drawerState.isOpen) { sheetVisible = false }

    ModalNavigationDrawer(
      gesturesEnabled = drawerState.isOpen,
      drawerState = drawerState,
      drawerContent = {
        ModalDrawerSheet {
          ModalSheetContent(
            modifier = Modifier.padding(20.dp).fillMaxHeight().verticalScroll(rememberScrollState())
          )
        }
      },
    ) {
      LocationScaffold()
    }
  }
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

      Column(modifier = Modifier.align(Alignment.BottomEnd).padding(20.dp)) {
        UserLocationButton(visible = !isLoading, onUserLocationClick = onUserLocationClick)

        Spacer(modifier = Modifier.height(10.dp))

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
    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent),
    title = {
      Text(
        text =
          mapPosition.label.takeIf(String::isNotEmpty)
            ?: stringResource(commonR.string.new_location),
        style = appBarTextStyle(),
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        textAlign = TextAlign.Center,
        modifier = Modifier.basicMarquee(iterations = Int.MAX_VALUE).padding(horizontal = 10.dp),
      )
    },
    navigationIcon = {
      SmallFloatingActionButton(
        onClick = onBackClick,
        modifier = Modifier.padding(start = 5.dp, top = 5.dp),
      ) {
        Icon(
          imageVector = Icons.Filled.ArrowBack,
          contentDescription = stringResource(commonR.string.back),
          tint = MaterialTheme.colorScheme.onSurface,
        )
      }
    },
    actions = {
      SmallFloatingActionButton(
        onClick = onInfoClick,
        modifier = Modifier.padding(end = 5.dp, top = 5.dp),
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
  FloatingActionButton(onClick = onClick) {
    Icon(imageVector = imageVector, contentDescription = stringResource(R.string.save_location))
  }
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
  val context = LocalContext.current

  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Top,
    modifier = modifier,
  ) {
    Text(
      text = headerLabel,
      style = MaterialTheme.typography.headlineMedium,
      maxLines = 2,
      modifier = Modifier.padding(10.dp).fillMaxWidth(),
    )

    TextField(
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
      modifier = Modifier.padding(10.dp).fillMaxWidth(),
    )

    LoadingProgressIndicator(
      visible = isNameLoading,
      modifier = Modifier.padding(horizontal = 10.dp).fillMaxWidth(),
    )

    AnimatedVisibility(
      visible = nameError != LocationNameError.NO_ERROR,
      enter = fadeIn(),
      exit = fadeOut(),
    ) {
      Text(
        modifier = Modifier.padding(horizontal = 10.dp),
        text =
          when (nameError) {
            LocationNameError.BLANK -> stringResource(R.string.location_name_cannot_be_blank)
            LocationNameError.NO_ERROR -> ""
          },
        fontSize = 14.sp,
        color = Color.Red,
      )
    }

    if (
      LocalHeightSizeClass.current == WindowHeightSizeClass.Compact ||
        LocalWidthSizeClass.current == WindowWidthSizeClass.Compact
    ) {
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

    Spacer(
      modifier =
        Modifier.height(
          10.dp + with(LocalDensity.current) { context.bottomNavigationBarInsetPx.toDp() }
        )
    )
  }

  ToastMessageEffect(message = nameFailureMessage)
}

@Composable
private fun ModalSheetButtonsRow(
  geocodeButtonText: String,
  onGeocodeClick: () -> Unit,
  onSaveClick: () -> Unit,
) {
  Row(modifier = Modifier.fillMaxWidth().padding(10.dp)) {
    OutlinedButton(onClick = onGeocodeClick, modifier = Modifier.weight(.5f)) {
      Text(
        text = geocodeButtonText,
        maxLines = 1,
        modifier = Modifier.basicMarquee(iterations = Int.MAX_VALUE),
      )
    }

    Spacer(modifier = Modifier.width(5.dp))

    OutlinedButton(onClick = onSaveClick, modifier = Modifier.weight(.5f)) {
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
    modifier = Modifier.fillMaxWidth().padding(top = 10.dp, start = 10.dp, end = 10.dp),
  ) {
    Text(
      text = geocodeButtonText,
      maxLines = 1,
      modifier = Modifier.basicMarquee(iterations = Int.MAX_VALUE),
    )
  }

  Spacer(modifier = Modifier.height(5.dp))

  OutlinedButton(
    onClick = onSaveClick,
    modifier = Modifier.fillMaxWidth().padding(start = 10.dp, end = 10.dp, bottom = 10.dp),
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
  val context = LocalContext.current
  return rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
    permissionsMap ->
    val allGranted = permissionsMap.values.all { it }
    if (allGranted) {
      Timber.tag("USER_LOCATION_PERMISSIONS").d("Location permissions are granted.")
      userLocationRequestState.shouldCheckIfLocationEnabled = true
    } else {
      val shouldShowRationale =
        userLocationRequestState.locationPermissions.any { permission ->
          ActivityCompat.shouldShowRequestPermissionRationale(
            requireNotNull(context.getActivity()),
            permission,
          )
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
