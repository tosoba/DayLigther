package com.trm.daylighter.widget.ui

import android.graphics.drawable.Icon
import android.util.TypedValue
import android.widget.RemoteViews
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.appwidget.AndroidRemoteViews
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.padding
import androidx.glance.layout.wrapContentSize
import com.trm.daylighter.core.common.R as commonR
import com.trm.daylighter.core.common.util.ext.dayLengthDiffPrefix
import com.trm.daylighter.core.common.util.ext.dayLengthDiffTime
import com.trm.daylighter.core.common.util.ext.formatTimeMillis
import com.trm.daylighter.core.domain.model.LocationSunriseSunsetChange
import com.trm.daylighter.core.domain.util.ext.dayLengthSecondsAtLocation
import com.trm.daylighter.core.ui.theme.light_onDayColor
import com.trm.daylighter.widget.R

@Composable
internal fun DayLengthInfo(change: LocationSunriseSunsetChange) {
  val context = LocalContext.current
  val (location, today, yesterday) = change

  val todayLengthSeconds = today.dayLengthSecondsAtLocation(location)
  val yesterdayLengthSeconds = yesterday.dayLengthSecondsAtLocation(location)
  val dayLengthDiffTime = dayLengthDiffTime(todayLengthSeconds, yesterdayLengthSeconds)
  val diffPrefix =
    dayLengthDiffPrefix(
      todayLengthSeconds = todayLengthSeconds,
      yesterdayLengthSeconds = yesterdayLengthSeconds
    )

  Row(
    horizontalAlignment = Alignment.Horizontal.CenterHorizontally,
    verticalAlignment = Alignment.Vertical.CenterVertically
  ) {
    DayLengthIcon()

    Column(
      verticalAlignment = Alignment.Vertical.CenterVertically,
      horizontalAlignment = Alignment.Horizontal.End
    ) {
      AndroidRemoteViews(
        modifier = GlanceModifier.wrapContentSize(),
        remoteViews =
          RemoteViews(context.packageName, R.layout.shadow_text_remote_view).apply {
            setTextViewText(R.id.shadow_text_view, formatTimeMillis(todayLengthSeconds * 1_000L))
            setInt(R.id.shadow_text_view, "setTextColor", light_onDayColor.toArgb())
            setTextViewTextSize(R.id.shadow_text_view, TypedValue.COMPLEX_UNIT_SP, 14f)
          }
      )

      AndroidRemoteViews(
        modifier = GlanceModifier.wrapContentSize(),
        remoteViews =
          RemoteViews(context.packageName, R.layout.shadow_text_remote_view).apply {
            setTextViewText(
              R.id.shadow_text_view,
              context.getString(
                R.string.diff,
                diffPrefix,
                dayLengthDiffTime.minute,
                dayLengthDiffTime.second
              ),
            )
            setInt(
              R.id.shadow_text_view,
              "setTextColor",
              when (diffPrefix) {
                "+" -> Color.Green
                "-" -> Color.Red
                else -> light_onDayColor
              }.toArgb()
            )
            setTextViewTextSize(R.id.shadow_text_view, TypedValue.COMPLEX_UNIT_SP, 14f)
          }
      )
    }
  }
}

@Composable
private fun DayLengthIcon() {
  val context = LocalContext.current
  Box {
    Image(
      provider =
        ImageProvider(Icon.createWithResource(context, commonR.drawable.day_length_shadow)),
      contentDescription = null,
      modifier = GlanceModifier.padding(start = 1.dp, top = 1.dp)
    )

    Image(
      provider = ImageProvider(Icon.createWithResource(context, commonR.drawable.day_length_white)),
      contentDescription = stringResource(R.string.day_length)
    )
  }
}
