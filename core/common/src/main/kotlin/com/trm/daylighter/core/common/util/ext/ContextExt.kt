package com.trm.daylighter.core.common.util.ext

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.IntentSender
import android.content.pm.PackageManager
import androidx.activity.result.IntentSenderRequest
import androidx.core.content.ContextCompat
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.Priority
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

fun Context.getActivity(): Activity? =
  when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.getActivity()
    else -> null
  }

fun Context.checkPermissions(
  permissions: Array<String>,
  onNotGranted: () -> Unit,
  onGranted: () -> Unit
) {
  val allGranted =
    permissions.all { permission ->
      ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
    }
  if (allGranted) onGranted() else onNotGranted()
}

suspend fun Context.checkLocationSettings(): CheckLocationSettingsResult {
  val locationRequest =
    LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 0L)
      .setMinUpdateIntervalMillis(0L)
      .setMaxUpdates(1)
      .build()

  return suspendCoroutine { continuation ->
    LocationServices.getSettingsClient(this)
      .checkLocationSettings(
        LocationSettingsRequest.Builder().addLocationRequest(locationRequest).build()
      )
      .addOnSuccessListener { continuation.resume(CheckLocationSettingsResult.Enabled) }
      .addOnFailureListener { exception ->
        if (exception is ResolvableApiException) {
          try {
            continuation.resume(
              CheckLocationSettingsResult.DisabledResolvable(
                IntentSenderRequest.Builder(exception.resolution).build()
              )
            )
          } catch (sendEx: IntentSender.SendIntentException) {
            continuation.resume(CheckLocationSettingsResult.DisabledNonResolvable)
          }
        } else {
          continuation.resume(CheckLocationSettingsResult.DisabledNonResolvable)
        }
      }
  }
}

sealed interface CheckLocationSettingsResult {
  object Enabled : CheckLocationSettingsResult

  data class DisabledResolvable(
    val intentSenderRequest: IntentSenderRequest,
  ) : CheckLocationSettingsResult

  object DisabledNonResolvable : CheckLocationSettingsResult
}
