package com.trm.daylighter.core.common.navigation

import android.appwidget.AppWidgetManager
import android.content.Context
import android.net.Uri
import androidx.core.net.toUri
import com.trm.daylighter.core.common.R

fun Context.newLocationDeepLinkPattern(): String =
  "${getString(R.string.deep_link_scheme)}://${getString(R.string.deep_link_host)}/location"

fun Context.newLocationDeeplinkUri(): Uri = newLocationDeepLinkPattern().toUri()

const val DAY_NIGHT_CYCLE_PATH_SEGMENT = "day_night_cycle"
const val GOLDEN_BLUE_HOUR_PATH_SEGMENT = "golden_blue_hour"
const val WIDGET_LOCATION_PATH_SEGMENT = "widget"

fun Context.dayNightCycleDeepLinkPattern(): String =
  "${getString(R.string.deep_link_scheme)}://${getString(R.string.deep_link_host)}/$DAY_NIGHT_CYCLE_PATH_SEGMENT/{${DayNightCycleRouteParams.LOCATION_ID}}?default={${DayNightCycleRouteParams.DEFAULT}}"

fun Context.dayNightCycleDeepLinkUri(locationId: Long, isDefault: Boolean): Uri =
  "${getString(R.string.deep_link_scheme)}://${getString(R.string.deep_link_host)}/$DAY_NIGHT_CYCLE_PATH_SEGMENT/${locationId}?default=${isDefault}"
    .toUri()

object DayNightCycleRouteParams {
  const val LOCATION_ID = "day_night_cycle_location_id"
  const val DEFAULT = "day_night_cycle_default"
}

fun Context.goldenBlueHourDeepLinkPattern(): String =
  "${getString(R.string.deep_link_scheme)}://${getString(R.string.deep_link_host)}/$GOLDEN_BLUE_HOUR_PATH_SEGMENT/{${GoldenBlueHourRouteParams.LOCATION_ID}}?default={${GoldenBlueHourRouteParams.DEFAULT}}"

fun Context.goldenBlueHourDeepLinkUri(locationId: Long, isDefault: Boolean): Uri =
  "${getString(R.string.deep_link_scheme)}://${getString(R.string.deep_link_host)}/$GOLDEN_BLUE_HOUR_PATH_SEGMENT/${locationId}?default=${isDefault}"
    .toUri()

object GoldenBlueHourRouteParams {
  const val LOCATION_ID = "golden_blue_hour_location_id"
  const val DEFAULT = "golden_blue_hour_default"
}

fun Context.widgetLocationDeepLinkPattern(): String =
  "${getString(R.string.deep_link_scheme)}://${getString(R.string.deep_link_host)}/$WIDGET_LOCATION_PATH_SEGMENT/{${WidgetLocationRouteParams.WIDGET_TYPE}}/{${AppWidgetManager.EXTRA_APPWIDGET_ID}}/location/{${WidgetLocationRouteParams.LOCATION_ID}}"

fun Context.widgetLocationDeepLinkUri(type: WidgetType, widgetId: Int, locationId: Long): Uri =
  "${getString(R.string.deep_link_scheme)}://${getString(R.string.deep_link_host)}/$WIDGET_LOCATION_PATH_SEGMENT/${type.name.lowercase()}/${widgetId}/location/${locationId}"
    .toUri()

object WidgetLocationRouteParams {
  const val WIDGET_TYPE = "widget_type"
  const val LOCATION_ID = "widget_location_location_id"
}

enum class WidgetType {
  DAY_NIGHT_CYCLE,
  GOLDEN_BLUE_HOUR;

  companion object {
    fun fromName(name: String): WidgetType =
      requireNotNull(entries.find { it.name.equals(name, ignoreCase = true) })
  }
}
