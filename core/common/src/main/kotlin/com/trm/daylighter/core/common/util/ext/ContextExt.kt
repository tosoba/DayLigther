package com.trm.daylighter.core.common.util.ext

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Location
import android.widget.Toast
import androidx.activity.result.IntentSenderRequest
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.tasks.CancellationTokenSource
import com.trm.daylighter.core.common.R as commonR
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlinx.coroutines.tasks.await

fun Context.getActivity(): Activity? =
  when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.getActivity()
    else -> null
  }

fun Context.goToUrlInBrowser(url: String) {
  try {
    startActivity(Intent(Intent.ACTION_VIEW, url.toUri()))
  } catch (_: ActivityNotFoundException) {
    Toast.makeText(this, getString(commonR.string.browser_app_was_not_found), Toast.LENGTH_SHORT)
      .show()
  }
}

fun Context.checkPermissions(
  permissions: Array<String>,
  onNotGranted: () -> Unit,
  onGranted: () -> Unit,
) {
  if (
    permissions.all { permission ->
      ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
    }
  ) {
    onGranted()
  } else {
    onNotGranted()
  }
}

suspend fun Context.checkLocationSettings(): CheckLocationSettingsResult =
  suspendCoroutine { continuation ->
    LocationServices.getSettingsClient(this)
      .checkLocationSettings(
        LocationSettingsRequest.Builder()
          .addLocationRequest(
            LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 0L)
              .setMinUpdateIntervalMillis(0L)
              .setMaxUpdates(1)
              .build()
          )
          .build()
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
          } catch (ex: IntentSender.SendIntentException) {
            continuation.resume(CheckLocationSettingsResult.DisabledNonResolvable)
          }
        } else {
          continuation.resume(CheckLocationSettingsResult.DisabledNonResolvable)
        }
      }
  }

sealed interface CheckLocationSettingsResult {
  data object Enabled : CheckLocationSettingsResult

  data class DisabledResolvable(val intentSenderRequest: IntentSenderRequest) :
    CheckLocationSettingsResult

  data object DisabledNonResolvable : CheckLocationSettingsResult
}

@SuppressLint("MissingPermission")
suspend fun Context.getCurrentUserLocation(): Location? {
  val cancellationTokenSource = CancellationTokenSource()
  return LocationServices.getFusedLocationProviderClient(this)
    .getCurrentLocation(
      CurrentLocationRequest.Builder()
        .setGranularity(Granularity.GRANULARITY_PERMISSION_LEVEL)
        .setPriority(Priority.PRIORITY_BALANCED_POWER_ACCURACY)
        .setDurationMillis(5_000L)
        .setMaxUpdateAgeMillis(60_000L)
        .build(),
      cancellationTokenSource.token,
    )
    .await(cancellationTokenSource)
}
