package com.trm.daylighter.core.common.navigation

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
  "${getString(R.string.deep_link_scheme)}://${getString(R.string.deep_link_host)}/$DAY_NIGHT_CYCLE_PATH_SEGMENT/{${DayNightCycleDeepLinkParams.LOCATION_ID}}?default={${DayNightCycleDeepLinkParams.DEFAULT}}"

fun Context.dayNightCycleDeepLinkUri(locationId: Long, isDefault: Boolean): Uri =
  "${getString(R.string.deep_link_scheme)}://${getString(R.string.deep_link_host)}/$DAY_NIGHT_CYCLE_PATH_SEGMENT/${locationId}?default=${isDefault}".toUri()

object DayNightCycleDeepLinkParams {
  const val LOCATION_ID = "day_night_cycle_location_id"
  const val DEFAULT = "day_night_cycle_default"
}

fun Context.goldenBlueHourDeepLinkPattern(): String =
  "${getString(R.string.deep_link_scheme)}://${getString(R.string.deep_link_host)}/$GOLDEN_BLUE_HOUR_PATH_SEGMENT/{${GoldenBlueHourDeepLinkParams.LOCATION_ID}}?default={${GoldenBlueHourDeepLinkParams.DEFAULT}}"

fun Context.goldenBlueHourDeepLinkUri(locationId: Long, isDefault: Boolean): Uri =
  "${getString(R.string.deep_link_scheme)}://${getString(R.string.deep_link_host)}/$GOLDEN_BLUE_HOUR_PATH_SEGMENT/${locationId}?default=${isDefault}".toUri()

object GoldenBlueHourDeepLinkParams {
  const val LOCATION_ID = "golden_blue_hour_location_id"
  const val DEFAULT = "golden_blue_hour_default"
}

fun Context.widgetLocationDeepLinkPattern(): String =
  "${getString(R.string.deep_link_scheme)}://${getString(R.string.deep_link_host)}/$WIDGET_LOCATION_PATH_SEGMENT/{${WidgetLocationDeepLinkParams.WIDGET_TYPE}}/{${WidgetLocationDeepLinkParams.GLANCE_ID}}/location/{${WidgetLocationDeepLinkParams.LOCATION_ID}}"

fun Context.widgetLocationDeepLinkUri(type: WidgetTypeParam, glanceId: Int, locationId: Long): Uri =
  "${getString(R.string.deep_link_scheme)}://${getString(R.string.deep_link_host)}/$WIDGET_LOCATION_PATH_SEGMENT/${type.name.lowercase()}/${glanceId}/location/${locationId}".toUri()

object WidgetLocationDeepLinkParams {
  const val WIDGET_TYPE = "widget_type"
  const val GLANCE_ID = "widget_location_glance_id"
  const val LOCATION_ID = "widget_location_location_id"
}

enum class WidgetTypeParam {
  DAY_NIGHT_CYCLE,
  GOLDEN_BLUE_HOUR;

  companion object {
    fun fromName(name: String): WidgetTypeParam =
      requireNotNull(WidgetTypeParam.values().find { it.name.lowercase() == name.lowercase() })
  }
}
