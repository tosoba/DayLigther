package com.trm.daylighter.widget.ui

import android.widget.RemoteViews
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.toArgb
import androidx.glance.LocalContext
import androidx.glance.appwidget.AndroidRemoteViews
import androidx.glance.layout.Box
import com.trm.daylighter.core.ui.theme.light_onDayColor
import com.trm.daylighter.widget.R
import java.time.ZoneId

@Composable
internal fun Clock(zoneId: ZoneId) {
  Box {
    AndroidRemoteViews(
      remoteViews =
        RemoteViews(LocalContext.current.packageName, R.layout.location_text_clock_remote_view)
          .apply {
            setString(R.id.location_clock, "setTimeZone", zoneId.id)
            setInt(R.id.location_clock, "setTextColor", light_onDayColor.toArgb())
          }
    )
  }
}
