package com.trm.daylighter.feature.location

import android.Manifest
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.trm.daylighter.core.common.R as commonR
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
  val isLoading = viewModel.isLoadingFlow.collectAsStateWithLifecycle(initialValue = false)

  LocationScreen(
    mapPosition = mapPosition.value,
    isLoading = isLoading.value,
    onSaveLocationClick = viewModel::saveLocation,
    onBackClick = onBackClick,
    modifier = modifier
  )
}

@Composable
private fun LocationScreen(
  mapPosition: MapPosition,
  isLoading: Boolean,
  onSaveLocationClick: (lat: Double, lng: Double) -> Unit,
  onBackClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  var savedMapPosition by rememberSaveable(mapPosition) { mutableStateOf(mapPosition) }
  var infoExpanded by rememberSaveable { mutableStateOf(true) }
  var locationProcessingInProgress by rememberSaveable { mutableStateOf(false) }
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
        FloatingActionButton(onClick = { locationProcessingInProgress = true }) {
          Icon(
            imageVector = Icons.Filled.MyLocation,
            contentDescription = stringResource(R.string.my_location),
          )
        }
        Spacer(modifier = Modifier.height(10.dp))
        FloatingActionButton(
          onClick = { onSaveLocationClick(mapView.mapCenter.latitude, mapView.mapCenter.longitude) }
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

    if (locationProcessingInProgress) {
      var dialogVisible by rememberSaveable { mutableStateOf(false) }
      RequestLocationPermission(
        dialogVisible = dialogVisible,
        modifier = Modifier.align(Alignment.Center).wrapContentHeight(),
        onGranted = {
          Timber.tag("LOCATION").e("Granted.")
          locationProcessingInProgress = false
        },
        onNotGranted = { dialogVisible = true },
        onDismiss = { locationProcessingInProgress = false }
      )
    }
  }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
private fun RequestLocationPermission(
  dialogVisible: Boolean,
  modifier: Modifier = Modifier,
  onGranted: () -> Unit,
  onNotGranted: () -> Unit,
  onDismiss: () -> Unit
) {
  val locationPermissionsState =
    rememberMultiplePermissionsState(
      listOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION)
    )

  AnimatedVisibility(visible = dialogVisible) {
    AlertDialog(
      modifier = modifier,
      onDismissRequest = onDismiss,
      confirmButton = {
        TextButton(onClick = locationPermissionsState::launchMultiplePermissionRequest) {
          Text(text = stringResource(id = android.R.string.ok))
        }
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
            if (locationPermissionsState.shouldShowRationale) {
              stringResource(R.string.location_permissions_dialog_rationale_text)
            } else {
              stringResource(R.string.location_permissions_dialog_text)
            }
        )
      }
    )
  }

  fun atLeastOnePermissionGranted() =
    locationPermissionsState.revokedPermissions.size != locationPermissionsState.permissions.size

  LaunchedEffect(atLeastOnePermissionGranted()) {
    if (atLeastOnePermissionGranted()) onGranted() else onNotGranted()
  }
}
