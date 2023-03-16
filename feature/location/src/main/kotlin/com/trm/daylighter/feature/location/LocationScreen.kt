package com.trm.daylighter.feature.location

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.trm.daylighter.core.ui.composable.rememberMapViewWithLifecycle
import com.trm.daylighter.feature.location.model.MapPosition
import com.trm.daylighter.feature.location.util.restorePosition
import com.trm.daylighter.feature.location.util.setDefaultConfig
import org.osmdroid.events.MapListener
import org.osmdroid.events.ScrollEvent
import org.osmdroid.events.ZoomEvent
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

  LocationScreen(
    mapPosition = mapPosition.value,
    isLoading = isLoading.value,
    onSaveLocationClick = viewModel::saveLocation,
    getAndSaveUserLocation = viewModel::getAndSaveUserLocation,
    onBackClick = onBackClick,
    modifier = modifier
  )
}

private enum class PermissionRequestMode {
  PERMISSION_REQUEST_DIALOG,
  APP_DETAILS_SETTINGS
}

@Composable
private fun LocationScreen(
  mapPosition: MapPosition,
  isLoading: Boolean,
  onSaveLocationClick: (lat: Double, lng: Double) -> Unit,
  getAndSaveUserLocation: () -> Unit,
  onBackClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val locationPermissions =
    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
  var permissionInfoDialogVisible by rememberSaveable { mutableStateOf(false) }
  var permissionRequestMode by rememberSaveable {
    mutableStateOf(PermissionRequestMode.PERMISSION_REQUEST_DIALOG)
  }
  var userLocationRequestedAndPermissionsGranted by rememberSaveable { mutableStateOf(false) }

  val locationPermissionsRequestLauncher =
    rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
      permissionsMap ->
      val allGranted = permissionsMap.values.all { it }
      if (allGranted) {
        Timber.tag("USER_LOCATION_PERMISSIONS").d("Location permissions are granted.")
        userLocationRequestedAndPermissionsGranted = true
      } else {
        val shouldShowRationale =
          locationPermissions.any { permission ->
            ActivityCompat.shouldShowRequestPermissionRationale(
              requireNotNull(context.getActivity()),
              permission
            )
          }
        permissionRequestMode =
          if (shouldShowRationale) PermissionRequestMode.PERMISSION_REQUEST_DIALOG
          else PermissionRequestMode.APP_DETAILS_SETTINGS
        permissionInfoDialogVisible = true
      }
    }

  fun Context.checkAndRequestLocationPermissions() {
    checkPermissions(
      permissions = locationPermissions,
      onNotGranted = {
        when (permissionRequestMode) {
          PermissionRequestMode.PERMISSION_REQUEST_DIALOG -> {
            locationPermissionsRequestLauncher.launch(locationPermissions)
          }
          PermissionRequestMode.APP_DETAILS_SETTINGS -> {
            context.startActivity(
              Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                .setData(Uri.fromParts("package", context.packageName, null))
            )
          }
        }
      },
      onGranted = { userLocationRequestedAndPermissionsGranted = true }
    )
  }

  val locationSettingsResultRequest =
    rememberLauncherForActivityResult(
      contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { activityResult ->
      if (activityResult.resultCode == Activity.RESULT_OK) {
        Timber.tag("USER_LOCATION_DIALOG")
          .d("Location was enabled after showing location settings dialog.")
        getAndSaveUserLocation()
      } else {
        Timber.tag("USER_LOCATION_DIALOG")
          .d("Location was NOT enabled after showing location settings dialog.")
      }
    }

  LaunchedEffect(userLocationRequestedAndPermissionsGranted) {
    if (!userLocationRequestedAndPermissionsGranted) return@LaunchedEffect

    when (val result = context.checkLocationSettings()) {
      is CheckLocationSettingsResult.DisabledNonResolvable -> {
        Toast.makeText(
            context,
            R.string.location_disabled_non_resolvable_message,
            Toast.LENGTH_LONG
          )
          .show()
        Timber.tag("USER_LOCATION_CHECK").d("Location is disabled after check - NON resolvable.")
      }
      is CheckLocationSettingsResult.DisabledResolvable -> {
        locationSettingsResultRequest.launch(result.intentSenderRequest)
      }
      is CheckLocationSettingsResult.Enabled -> {
        Timber.tag("USER_LOCATION_CHECK").d("Location is enabled after check.")
        getAndSaveUserLocation()
      }
    }

    userLocationRequestedAndPermissionsGranted = false
  }

  var savedMapPosition by rememberSaveable(mapPosition) { mutableStateOf(mapPosition) }
  var infoExpanded by rememberSaveable { mutableStateOf(true) }

  val darkMode = isSystemInDarkTheme()

  val mapView =
    rememberMapViewWithLifecycle(
      onPause = {
        savedMapPosition =
          MapPosition(
            latitude = it.mapCenter.latitude,
            longitude = it.mapCenter.longitude,
            zoom = it.zoomLevelDouble,
            orientation = it.mapOrientation
          )
      }
    )
  val mapListener = remember {
    object : MapListener {
      override fun onScroll(event: ScrollEvent?): Boolean = onMapInteraction()
      override fun onZoom(event: ZoomEvent?): Boolean = onMapInteraction()
      private fun onMapInteraction(): Boolean {
        infoExpanded = false
        return false
      }
    }
  }

  Box(modifier = modifier) {
    AndroidView(
      factory = { mapView },
      update = {
        it.setDefaultConfig(darkMode = darkMode)
        it.removeMapListener(mapListener)
        it.restorePosition(savedMapPosition)
        it.addMapListener(mapListener)
      },
      modifier = Modifier.fillMaxSize(),
    )

    Icon(
      painter = painterResource(id = commonR.drawable.marker),
      contentDescription = stringResource(id = commonR.string.location_marker),
      modifier = Modifier.align(Alignment.Center)
    )

    AnimatedVisibility(
      visible = !isLoading,
      modifier = Modifier.align(Alignment.BottomEnd).padding(20.dp)
    ) {
      Column {
        FloatingActionButton(onClick = context::checkAndRequestLocationPermissions) {
          Icon(
            imageVector = Icons.Filled.MyLocation,
            contentDescription = stringResource(R.string.my_location),
          )
        }
        Spacer(modifier = Modifier.height(10.dp))
        FloatingActionButton(
          onClick = {
            val mapCenter = mapView.mapCenter
            onSaveLocationClick(mapCenter.latitude, mapCenter.longitude)
          }
        ) {
          Icon(
            imageVector = Icons.Filled.Done,
            contentDescription = stringResource(R.string.save_location)
          )
        }
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
            if (infoExpanded) MaterialTheme.colorScheme.background
            else FloatingActionButtonDefaults.containerColor
        )
      FloatingActionButton(
        modifier = Modifier.padding(start = 5.dp),
        containerColor = infoContainerColor.value,
        onClick = { infoExpanded = !infoExpanded }
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 16.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(
            imageVector = Icons.Filled.Info,
            contentDescription = stringResource(R.string.center_map_on_location)
          )
          AnimatedVisibility(visible = infoExpanded) {
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

    LocationPermissionInfoDialog(
      dialogVisible = permissionInfoDialogVisible,
      permissionRequestMode = permissionRequestMode,
      onOkClick = {
        permissionInfoDialogVisible = false
        context.checkAndRequestLocationPermissions()
      },
      onDismiss = { permissionInfoDialogVisible = false },
      modifier = Modifier.align(Alignment.Center).wrapContentHeight()
    )
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
