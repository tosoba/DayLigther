package com.trm.daylighter.feature.location

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
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
import com.trm.daylighter.feature.location.model.*
import com.trm.daylighter.feature.location.util.restorePosition
import com.trm.daylighter.feature.location.util.setDefaultConfig
import timber.log.Timber

const val locationRoute = "location_route"
const val locationIdParam = "location_id"
const val editLocationRoute = "$locationRoute/{$locationIdParam}"

@Composable
fun LocationRoute(
  onBackClick: () -> Unit,
  onEnableGeocodingClick: () -> Unit,
  modifier: Modifier = Modifier,
  viewModel: LocationViewModel = hiltViewModel()
) {
  LaunchedEffect(Unit) { viewModel.locationSavedFlow.collect { onBackClick() } }

  val mapPosition = viewModel.initialMapPositionFlow.collectAsStateWithLifecycle()
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
      else { _, _ -> onEnableGeocodingClick() },
    onBackClick = onBackClick,
    modifier = modifier
  )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LocationScreen(
  screenMode: LocationScreenMode,
  mapPosition: MapPosition,
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
  modifier: Modifier = Modifier
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
      onGranted = { userLocationRequestState.shouldCheckIfLocationEnabled = true }
    )
  }

  CheckLocationSettingsEffect(
    userLocationRequestState = userLocationRequestState,
    onLocationEnabled = requestGetAndSaveUserLocation
  )

  UserLocationNotFoundToastEffect(userLocationNotFound = userLocationNotFound)

  val orientation = LocalConfiguration.current.orientation
  val locationMap = rememberLocationMap(mapPosition = mapPosition)
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
          LocationScreenMode.ADD -> commonR.string.add_location
          LocationScreenMode.EDIT -> R.string.edit_location
        }
    )

  LaunchedEffect(locationPreparedToSave) {
    if (locationPreparedToSave == null || !locationPreparedToSave.isUser) return@LaunchedEffect
    sheetVisible = true
    locationMap.state.updatePosition(
      latitude = locationPreparedToSave.latitude,
      longitude = locationPreparedToSave.longitude
    )
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
      modifier = modifier
    )
  }

  @Composable
  fun LocationScaffold() {
    LocationScaffold(
      locationMap = locationMap,
      isLoading = isLoading,
      saveSpecifiedLocation = { latitude, longitude ->
        saveSpecifiedLocationClick(latitude, longitude)
        sheetVisible = true
      },
      onUserLocationClick = context::checkLocationPermissions,
      cancelCurrentSaveLocation = cancelCurrentSaveLocation,
      onBackClick = onBackClick,
      modalSheet = {
        if (sheetVisible && orientation == Configuration.ORIENTATION_PORTRAIT) {
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
          modifier = Modifier.align(Alignment.Center).wrapContentHeight()
        )
      },
      modifier = modifier,
    )
  }

  LaunchedEffect(sheetVisible) { if (!sheetVisible) clearLocationName() }

  if (orientation == Configuration.ORIENTATION_PORTRAIT) {
    LocationScaffold()
  } else {
    val drawerState = remember {
      DrawerState(initialValue = if (sheetVisible) DrawerValue.Open else DrawerValue.Closed)
    }
    LaunchedEffect(sheetVisible) { drawerState.run { if (sheetVisible) open() else close() } }
    LaunchedEffect(drawerState.currentValue) {
      if (drawerState.isClosed && !drawerState.isAnimationRunning) sheetVisible = false
    }

    ModalNavigationDrawer(
      gesturesEnabled = drawerState.isOpen,
      drawerState = drawerState,
      drawerContent = {
        ModalDrawerSheet { ModalSheetContent(modifier = Modifier.padding(20.dp).fillMaxHeight()) }
      }
    ) {
      LocationScaffold()
    }
  }
}

@Composable
private fun LocationScaffold(
  locationMap: LocationMap,
  isLoading: Boolean,
  saveSpecifiedLocation: (lat: Double, lng: Double) -> Unit,
  onUserLocationClick: () -> Unit,
  cancelCurrentSaveLocation: () -> Unit,
  onBackClick: () -> Unit,
  modalSheet: @Composable () -> Unit,
  locationPermissionDialog: @Composable BoxScope.() -> Unit,
  modifier: Modifier = Modifier
) {
  Scaffold(modifier = modifier) { padding ->
    Box(modifier = Modifier.fillMaxSize().padding(padding)) {
      MapView(locationMap = locationMap, modifier = Modifier.fillMaxSize())
      MarkerIcon(modifier = Modifier.align(Alignment.Center))

      Column(modifier = Modifier.align(Alignment.BottomEnd).padding(20.dp)) {
        UserLocationButton(visible = !isLoading, onUserLocationClick = onUserLocationClick)

        Spacer(modifier = Modifier.height(10.dp))

        SaveSpecifiedLocationButton(
          imageVector = if (!isLoading) Icons.Filled.Done else Icons.Filled.Cancel
        ) {
          if (!isLoading) {
            val mapCenter = locationMap.view.mapCenter
            saveSpecifiedLocation(mapCenter.latitude, mapCenter.longitude)
          } else {
            cancelCurrentSaveLocation()
          }
        }
      }

      val context = LocalContext.current
      var infoToast: Toast? by remember { mutableStateOf(null) }
      LocationAppBar(
        locationMap = locationMap,
        onBackClick = onBackClick,
        onInfoClick = {
          Toast.makeText(context, R.string.new_location_info, Toast.LENGTH_LONG).also {
            it.show()
            infoToast?.cancel()
            infoToast = it
          }
        }
      )

      LoadingProgressIndicator(
        visible = isLoading,
        modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth()
      )

      locationPermissionDialog()
    }

    modalSheet()
  }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
private fun LocationAppBar(
  locationMap: LocationMap,
  onBackClick: () -> Unit,
  onInfoClick: () -> Unit
) {
  CenterAlignedTopAppBar(
    modifier =
      Modifier.background(
        Brush.verticalGradient(0f to MaterialTheme.colorScheme.surface, 1f to Color.Transparent)
      ),
    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent),
    title = {
      Text(
        text = locationMap.state.savedMapPosition.label.takeIf(String::isNotEmpty)
            ?: stringResource(commonR.string.new_location),
        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Normal),
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        textAlign = TextAlign.Center,
        modifier = Modifier.basicMarquee().padding(horizontal = 10.dp)
      )
    },
    navigationIcon = {
      IconButton(onClick = onBackClick) {
        Icon(
          imageVector = Icons.Filled.ArrowBack,
          contentDescription = stringResource(commonR.string.back),
          tint = MaterialTheme.colorScheme.onSurface
        )
      }
    },
    actions = {
      IconButton(onClick = onInfoClick) {
        Icon(
          imageVector = Icons.Filled.Info,
          contentDescription = stringResource(R.string.new_location_info),
          tint = MaterialTheme.colorScheme.onSurface
        )
      }
    }
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
private fun MapView(locationMap: LocationMap, modifier: Modifier = Modifier) {
  val darkMode = isSystemInDarkTheme()
  AndroidView(
    factory = { locationMap.view },
    update = {
      it.setDefaultConfig(darkMode = darkMode)
      it.restorePosition(position = locationMap.state.savedMapPosition)
    },
    modifier = modifier,
  )
}

@Composable
private fun MarkerIcon(modifier: Modifier = Modifier) {
  Icon(
    painter = painterResource(id = commonR.drawable.marker),
    contentDescription = stringResource(commonR.string.location_marker),
    modifier = modifier
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
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current

  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Top,
    modifier = modifier
  ) {
    Text(
      text = headerLabel,
      style = MaterialTheme.typography.headlineMedium,
      maxLines = 2,
      modifier = Modifier.padding(10.dp).fillMaxWidth()
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
            modifier = Modifier.clickable { onNameValueChange("") }
          )
        }
      },
      modifier = Modifier.padding(10.dp).fillMaxWidth()
    )

    LoadingProgressIndicator(
      visible = isNameLoading,
      modifier = Modifier.padding(horizontal = 10.dp).fillMaxWidth()
    )

    AnimatedVisibility(
      visible = nameError != LocationNameError.NO_ERROR,
      enter = fadeIn(),
      exit = fadeOut()
    ) {
      Text(
        modifier = Modifier.padding(horizontal = 10.dp),
        text =
          when (nameError) {
            LocationNameError.BLANK -> stringResource(R.string.location_name_cannot_be_blank)
            LocationNameError.NO_ERROR -> ""
          },
        fontSize = 14.sp,
        color = Color.Red
      )
    }

    ModalSheetButtons(
      geocodeButtonText = geocodeButtonText,
      onGeocodeClick = onGeocodeClick,
      onSaveClick = onSaveClick
    )

    Spacer(
      modifier =
        Modifier.height(
          10.dp + with(LocalDensity.current) { context.bottomNavigationBarInsetPx.toDp() }
        )
    )
  }

  ToastMessageEffect(message = nameFailureMessage)
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ModalSheetButtons(
  geocodeButtonText: String,
  onGeocodeClick: () -> Unit,
  onSaveClick: () -> Unit
) {
  Row(modifier = Modifier.fillMaxWidth().padding(10.dp)) {
    OutlinedButton(onClick = onGeocodeClick, modifier = Modifier.weight(.5f)) {
      Text(text = geocodeButtonText, maxLines = 1, modifier = Modifier.basicMarquee())
    }
    Spacer(modifier = Modifier.width(5.dp))
    OutlinedButton(onClick = onSaveClick, modifier = Modifier.weight(.5f)) {
      Text(text = stringResource(R.string.save), maxLines = 1, modifier = Modifier.basicMarquee())
    }
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
  userLocationRequestState: UserLocationRequestState,
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
            permission
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
            Toast.LENGTH_LONG
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
  modifier: Modifier = Modifier
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
          textAlign = TextAlign.Center
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
      }
    )
  }
}
