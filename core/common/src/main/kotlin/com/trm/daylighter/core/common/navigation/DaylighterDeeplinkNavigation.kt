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
  const val LOCATION_ID = "day_locationId"
  const val DEFAULT = "day_default"
}

fun Context.widgetLocationDeepLinkPattern(): String =
  "${getString(R.string.deep_link_scheme)}://${getString(R.string.deep_link_host)}/widget/{${WidgetLocationDeepLinkParams.GLANCE_ID}}/location/{${WidgetLocationDeepLinkParams.LOCATION_ID}}"

fun Context.widgetLocationDeepLinkUri(glanceId: Int, locationId: Long): Uri =
  "${getString(R.string.deep_link_scheme)}://${getString(R.string.deep_link_host)}/widget/${glanceId}/location/${locationId}".toUri()

object WidgetLocationDeepLinkParams {
  const val GLANCE_ID = "widgetLocation_glanceId"
  const val LOCATION_ID = "widgetLocation_locationId"
}
