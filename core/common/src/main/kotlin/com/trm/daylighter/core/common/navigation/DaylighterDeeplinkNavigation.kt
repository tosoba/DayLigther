package com.trm.daylighter.core.common.navigation

import android.content.Context
import android.net.Uri
import androidx.core.net.toUri
import com.trm.daylighter.core.common.R

fun Context.addLocationDeepLinkPattern(): String =
  "${getString(R.string.deep_link_scheme)}://${getString(R.string.deep_link_host)}/location"

fun Context.addLocationDeeplinkUri(): Uri = addLocationDeepLinkPattern().toUri()

fun Context.dayNightCycleDeepLinkPattern(): String =
  "${getString(R.string.deep_link_scheme)}://${getString(R.string.deep_link_host)}/day_night_cycle/{${DayNightCycleDeepLinkParams.LOCATION_ID}}?default={${DayNightCycleDeepLinkParams.DEFAULT}}"

fun Context.dayNightCycleDeepLinkUri(locationId: Long, isDefault: Boolean): Uri =
  "${getString(R.string.deep_link_scheme)}://${getString(R.string.deep_link_host)}/day_night_cycle/${locationId}?default=${isDefault}".toUri()

object DayNightCycleDeepLinkParams {
  const val LOCATION_ID = "day_night_cycle_location_id"
  const val DEFAULT = "day_night_cycle_default"
}

fun Context.goldenBlueHourDeepLinkPattern(): String =
  "${getString(R.string.deep_link_scheme)}://${getString(R.string.deep_link_host)}/golden_blue_hour/{${GoldenBlueHourDeepLinkParams.LOCATION_ID}}?default={${GoldenBlueHourDeepLinkParams.DEFAULT}}"

fun Context.goldenBlueHourDeepLinkUri(locationId: Long, isDefault: Boolean): Uri =
  "${getString(R.string.deep_link_scheme)}://${getString(R.string.deep_link_host)}/golden_blue_hour/${locationId}?default=${isDefault}".toUri()

object GoldenBlueHourDeepLinkParams {
  const val LOCATION_ID = "golden_blue_hour_location_id"
  const val DEFAULT = "golden_blue_hour_default"
}

fun Context.widgetLocationDeepLinkPattern(): String =
  "${getString(R.string.deep_link_scheme)}://${getString(R.string.deep_link_host)}/widget/{${WidgetLocationDeepLinkParams.GLANCE_ID}}/location/{${WidgetLocationDeepLinkParams.LOCATION_ID}}"

fun Context.widgetLocationDeepLinkUri(glanceId: Int, locationId: Long): Uri =
  "${getString(R.string.deep_link_scheme)}://${getString(R.string.deep_link_host)}/widget/${glanceId}/location/${locationId}".toUri()

object WidgetLocationDeepLinkParams {
  const val WIDGET_TYPE = "widget_type"
  const val GLANCE_ID = "widget_location_glance_id"
  const val LOCATION_ID = "widget_location_location_id"
}

enum class WidgetTypeParam {
  DAY_NIGHT_CYCLE_WIDGET,
  GOLDEN_BLUE_HOUR_WIDGET
}
