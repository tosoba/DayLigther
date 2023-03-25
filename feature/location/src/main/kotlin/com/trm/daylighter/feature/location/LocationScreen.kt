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
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.trm.daylighter.core.common.R as commonR
import com.trm.daylighter.core.common.util.ext.CheckLocationSettingsResult
import com.trm.daylighter.core.common.util.ext.checkLocationSettings
import com.trm.daylighter.core.common.util.ext.checkPermissions
import com.trm.daylighter.core.common.util.ext.getActivity
import com.trm.daylighter.feature.location.model.*
import com.trm.daylighter.feature.location.util.restorePosition
import com.trm.daylighter.feature.location.util.setDefaultConfig
import eu.wewox.modalsheet.ExperimentalSheetApi
import eu.wewox.modalsheet.ModalSheet
import timber.log.Timber

const val locationRoute = "location_route"
const val locationIdParam = "location_id"
const val editLocationRoute = "$locationRoute/{$locationIdParam}"

@Composable
fun LocationRoute(
  onBackClick: () -> Unit,
  modifier: Modifier = Modifier,
  viewModel: LocationViewModel = hiltViewModel()
) {
  LaunchedEffect(Unit) { viewModel.savedFlow.collect { onBackClick() } }

  val mapPosition = viewModel.initialMapPositionFlow.collectAsStateWithLifecycle()
  val isLoading = viewModel.loadingFlow.collectAsStateWithLifecycle(initialValue = false)
  val userLocationNotFound =
    viewModel.userLocationNotFoundFlow.collectAsStateWithLifecycle(initialValue = false)

  LocationScreen(
    mapPosition = mapPosition.value,
    isLoading = isLoading.value,
    userLocationNotFound = userLocationNotFound.value,
    onSaveLocationClick = viewModel::saveSpecifiedLocation,
    cancelSaveLocation = viewModel::cancelSaveLocation,
    getAndSaveUserLocation = viewModel::getAndSaveUserLocation,
    onBackClick = onBackClick,
    modifier = modifier
  )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalSheetApi::class)
@Composable
private fun LocationScreen(
  mapPosition: MapPosition,
  isLoading: Boolean,
  userLocationNotFound: Boolean,
  onSaveLocationClick: (lat: Double, lng: Double) -> Unit,
  cancelSaveLocation: () -> Unit,
  getAndSaveUserLocation: () -> Unit,
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
    onLocationEnabled = getAndSaveUserLocation
  )

  UserLocationNotFoundToastEffect(userLocationNotFound = userLocationNotFound)

  val orientation = LocalConfiguration.current.orientation
  var sheetVisible by rememberSaveable { mutableStateOf(false) }
  val locationMap = rememberLocationMap(mapPosition = mapPosition)

  @Composable
  fun LocationScaffold() {
    LocationScaffold(
      isLoading = isLoading,
      onSaveLocationClick = onSaveLocationClick,
      cancelSaveLocation = cancelSaveLocation,
      onUserLocationClick = context::checkLocationPermissions,
      onBackClick = onBackClick,
      locationMap = locationMap,
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
      modalSheet = {
        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
          ModalSheet(
            visible = sheetVisible,
            onVisibleChange = { sheetVisible = it },
            backgroundColor = MaterialTheme.colorScheme.background,
            shape = MaterialTheme.shapes.medium,
          ) {
            ModalSheetContent(modifier = Modifier.padding(20.dp).fillMaxWidth())
          }
        }
      },
      modifier = modifier,
    )
  }

  if (orientation == Configuration.ORIENTATION_PORTRAIT) {
    LocationScaffold()
  } else {
    val drawerState = remember {
      DrawerState(initialValue = if (sheetVisible) DrawerValue.Open else DrawerValue.Closed)
    }
    LaunchedEffect(sheetVisible) { if (sheetVisible) drawerState.open() }
    LaunchedEffect(drawerState.currentValue) { if (drawerState.isClosed) sheetVisible = false }

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LocationScaffold(
  isLoading: Boolean,
  onSaveLocationClick: (lat: Double, lng: Double) -> Unit,
  cancelSaveLocation: () -> Unit,
  onUserLocationClick: () -> Unit,
  onBackClick: () -> Unit,
  locationMap: LocationMap,
  locationPermissionDialog: @Composable BoxScope.() -> Unit,
  modalSheet: @Composable () -> Unit,
  modifier: Modifier = Modifier
) {
  val darkMode = isSystemInDarkTheme()

  Scaffold(modifier = modifier) { padding ->
    Box(modifier = Modifier.fillMaxSize().padding(padding)) {
      AndroidView(
        factory = { locationMap.view },
        update = {
          it.setDefaultConfig(darkMode = darkMode)
          it.removeMapListener(locationMap.listener)
          it.restorePosition(position = locationMap.state.savedMapPosition)
          it.addMapListener(locationMap.listener)
        },
        modifier = Modifier.fillMaxSize(),
      )

      Icon(
        painter = painterResource(id = commonR.drawable.marker),
        contentDescription = stringResource(id = commonR.string.location_marker),
        modifier = Modifier.align(Alignment.Center)
      )

      Column(modifier = Modifier.align(Alignment.BottomEnd).padding(20.dp)) {
        AnimatedVisibility(visible = !isLoading) {
          FloatingActionButton(onClick = onUserLocationClick) {
            Icon(
              imageVector = Icons.Filled.MyLocation,
              contentDescription = stringResource(R.string.my_location),
            )
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        FloatingActionButton(
          onClick = {
            if (!isLoading) {
              locationMap.state.infoExpanded = false
              val mapCenter = locationMap.view.mapCenter
              onSaveLocationClick(mapCenter.latitude, mapCenter.longitude)
            } else {
              cancelSaveLocation()
            }
          }
        ) {
          Icon(
            imageVector = if (!isLoading) Icons.Filled.Done else Icons.Filled.Cancel,
            contentDescription = stringResource(R.string.save_location)
          )
        }
      }

      Row(modifier = Modifier.padding(20.dp)) {
        SmallFloatingActionButton(onClick = onBackClick, modifier = Modifier.padding(end = 5.dp)) {
          Icon(
            imageVector = Icons.Filled.ArrowBack,
            contentDescription = stringResource(id = commonR.string.back)
          )
        }

        Spacer(modifier = Modifier.weight(1f))

        val infoContainerColor =
          animateColorAsState(
            targetValue =
              if (locationMap.state.infoExpanded) MaterialTheme.colorScheme.background
              else FloatingActionButtonDefaults.containerColor
          )
        FloatingActionButton(
          modifier = Modifier.padding(start = 5.dp),
          containerColor = infoContainerColor.value,
          onClick = locationMap.state::toggleInfoExpanded
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              imageVector = Icons.Filled.Info,
              contentDescription = stringResource(R.string.center_map_on_location)
            )
            AnimatedVisibility(visible = locationMap.state.infoExpanded) {
              Row {
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                  text = stringResource(R.string.center_map_on_location),
                  style = MaterialTheme.typography.bodyLarge,
                  textAlign = TextAlign.Center
                )
              }
            }
          }
        }
      }

      AnimatedVisibility(
        visible = isLoading,
        modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth()
      ) {
        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
      }

      locationPermissionDialog()
    }

    modalSheet()
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ModalSheetContent(modifier: Modifier = Modifier) {
  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center,
    modifier = modifier
  ) {
    TextField(value = "KEKW", onValueChange = {})
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
  var userLocationNotFoundToast: Toast? by remember { mutableStateOf(null) }

  LaunchedEffect(userLocationNotFound) {
    userLocationNotFoundToast =
      if (userLocationNotFound) {
        Toast.makeText(context, R.string.location_not_found, Toast.LENGTH_LONG).apply { show() }
      } else {
        userLocationNotFoundToast?.cancel()
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
  AnimatedVisibility(visible = dialogVisible) {
    AlertDialog(
      modifier = modifier,
      onDismissRequest = onDismiss,
      confirmButton = {
        TextButton(onClick = onOkClick) { Text(text = stringResource(id = android.R.string.ok)) }
      },
      dismissButton = {
        TextButton(onClick = onDismiss) {
          Text(text = stringResource(id = android.R.string.cancel))
        }
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
