package com.trm.daylighter.core.common.navigation

import android.content.Context
import android.net.Uri
import androidx.core.net.toUri
import com.trm.daylighter.core.common.R

fun Context.addLocationDeepPattern(): String =
  "${getString(R.string.deep_link_scheme)}://${getString(R.string.deep_link_host)}/location"

fun Context.addLocationDeeplinkUri(): Uri = addLocationDeepPattern().toUri()

fun Context.dayDeepLinkPattern(): String =
  "${getString(R.string.deep_link_scheme)}://${getString(R.string.deep_link_host)}/day/{${DayDeepLinkParams.LOCATION_ID}}?default={${DayDeepLinkParams.DEFAULT}}"

fun Context.dayDeepLinkUri(locationId: Long, isDefault: Boolean): Uri =
  "${getString(R.string.deep_link_scheme)}://${getString(R.string.deep_link_host)}/day/${locationId}?default=${isDefault}".toUri()

object DayDeepLinkParams {
  const val LOCATION_ID = "locationId"
  const val DEFAULT = "default"
}
