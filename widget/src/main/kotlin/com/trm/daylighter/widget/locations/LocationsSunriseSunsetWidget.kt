package com.trm.daylighter.widget.locations

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.glance.BitmapImageProvider
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.LocalSize
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.currentState
import androidx.glance.layout.Box
import androidx.glance.layout.ContentScale
import androidx.glance.layout.fillMaxSize
import com.trm.daylighter.core.domain.model.Loadable
import com.trm.daylighter.core.domain.model.LocationSunriseSunsetChange
import com.trm.daylighter.core.ui.theme.dayColor
import com.trm.daylighter.core.ui.theme.nightColor
import com.trm.daylighter.widget.ui.GlanceTheme
import com.trm.daylighter.widget.ui.appWidgetBackgroundCornerRadius
import com.trm.daylighter.widget.ui.toPx

class LocationsSunriseSunsetWidget : GlanceAppWidget() {
  override val stateDefinition = LocationsSunriseSunsetWidgetStateDefinition
  override val sizeMode: SizeMode = SizeMode.Responsive(setOf(wideMode))

  private val nightPaint by
    lazy(LazyThreadSafetyMode.NONE) {
      Paint(Paint.ANTI_ALIAS_FLAG).apply { color = nightColor.toArgb() }
    }
  private val dayPaint by
    lazy(LazyThreadSafetyMode.NONE) {
      Paint(Paint.ANTI_ALIAS_FLAG).apply { color = dayColor.toArgb() }
    }

  @Composable
  override fun Content() {
    GlanceTheme {
      Box(modifier = GlanceModifier.fillMaxSize().appWidgetBackgroundCornerRadius()) {
        Image(
          provider = BitmapImageProvider(createDayBitmap()),
          contentDescription = null,
          contentScale = ContentScale.FillBounds,
          modifier = GlanceModifier.fillMaxSize()
        )
      }
      when (val changes = currentState<Loadable<List<LocationSunriseSunsetChange>>>()) {
        else -> {}
      //        Empty -> AddLocationButton()
      //        else -> AddLocationButton() // TODO:
      }
    }
  }

  @Composable
  private fun createDayBitmap(): Bitmap {
    val size = LocalSize.current
    val widthPx = size.width.value.toPx
    val heightPx = size.height.value.toPx
    val bitmap = Bitmap.createBitmap(widthPx.toInt(), heightPx.toInt(), Bitmap.Config.ARGB_8888)
    Canvas(bitmap).apply {
      drawRect(0f, 0f, widthPx / 2, heightPx, nightPaint)
      drawRect(widthPx / 2, 0f, widthPx, heightPx, dayPaint)
    }
    return bitmap
  }

  companion object {
    private val wideMode = DpSize(200.dp, 50.dp)
  }
}
