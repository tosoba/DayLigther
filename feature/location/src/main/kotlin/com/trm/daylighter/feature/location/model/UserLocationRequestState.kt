package com.trm.daylighter.feature.location.model

import android.Manifest
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable

@Composable
internal fun rememberUserLocationRequestState(
  shouldCheckIfLocationEnabled: Boolean = false,
  permissionInfoDialogVisible: Boolean = false,
  permissionRequestMode: PermissionRequestMode = PermissionRequestMode.PERMISSION_REQUEST_DIALOG,
): UserLocationRequestState =
  rememberSaveable(saver = UserLocationRequestState.Saver) {
    UserLocationRequestState(
      shouldCheckIfLocationEnabled = shouldCheckIfLocationEnabled,
      permissionInfoDialogVisible = permissionInfoDialogVisible,
      permissionRequestMode = permissionRequestMode,
    )
  }

@Stable
internal class UserLocationRequestState(
  shouldCheckIfLocationEnabled: Boolean = false,
  permissionInfoDialogVisible: Boolean = false,
  permissionRequestMode: PermissionRequestMode = PermissionRequestMode.PERMISSION_REQUEST_DIALOG,
) {
  var shouldCheckIfLocationEnabled by mutableStateOf(shouldCheckIfLocationEnabled)
  var permissionInfoDialogVisible by mutableStateOf(permissionInfoDialogVisible)
  var permissionRequestMode by mutableStateOf(permissionRequestMode)

  val locationPermissions =
    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)

  companion object {
    val Saver: Saver<UserLocationRequestState, *> =
      listSaver(
        save = {
          listOf<Any>(
            it.shouldCheckIfLocationEnabled,
            it.permissionInfoDialogVisible,
            it.permissionRequestMode,
          )
        },
        restore = {
          UserLocationRequestState(
            shouldCheckIfLocationEnabled = it[0] as Boolean,
            permissionInfoDialogVisible = it[1] as Boolean,
            permissionRequestMode = it[2] as PermissionRequestMode,
          )
        },
      )
  }
}
