package com.trm.daylighter.widget.ui

import android.util.TypedValue
import android.widget.RemoteViews
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.toArgb
import androidx.glance.GlanceModifier
import androidx.glance.LocalContext
import androidx.glance.LocalSize
import androidx.glance.appwidget.AndroidRemoteViews
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.width
import com.trm.daylighter.core.domain.model.Location
import com.trm.daylighter.core.ui.theme.light_onDayColor
import com.trm.daylighter.widget.R

@Composable
internal fun LocationName(location: Location) {
  val context = LocalContext.current
  val size = LocalSize.current
  Box(contentAlignment = Alignment.Center, modifier = GlanceModifier.width(size.width * .75f)) {
    AndroidRemoteViews(
      remoteViews =
        RemoteViews(context.packageName, R.layout.shadow_text_remote_view).apply {
          setCharSequence(R.id.shadow_text_view, "setText", location.name)
          setInt(R.id.shadow_text_view, "setTextColor", light_onDayColor.toArgb())
          setTextViewTextSize(R.id.shadow_text_view, TypedValue.COMPLEX_UNIT_SP, smallFontSize)
        }
    )
  }
}
