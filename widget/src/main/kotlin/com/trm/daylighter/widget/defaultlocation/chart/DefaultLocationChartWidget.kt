package com.trm.daylighter.widget.defaultlocation.chart

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
import androidx.glance.LocalContext
import androidx.glance.LocalSize
import androidx.glance.appwidget.CircularProgressIndicator
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.actionSendBroadcast
import androidx.glance.currentState
import androidx.glance.layout.Box
import androidx.glance.layout.ContentScale
import androidx.glance.layout.fillMaxSize
import com.trm.daylighter.core.domain.model.Empty
import com.trm.daylighter.core.domain.model.Failed
import com.trm.daylighter.core.domain.model.Loadable
import com.trm.daylighter.core.domain.model.Loading
import com.trm.daylighter.core.domain.model.LocationSunriseSunsetChange
import com.trm.daylighter.core.domain.model.Ready
import com.trm.daylighter.core.domain.model.SunriseSunset
import com.trm.daylighter.core.ui.theme.astronomicalTwilightColor
import com.trm.daylighter.core.ui.theme.civilTwilightColor
import com.trm.daylighter.core.ui.theme.dayColor
import com.trm.daylighter.core.ui.theme.nauticalTwilightColor
import com.trm.daylighter.core.ui.theme.nightColor
import com.trm.daylighter.widget.ui.AddLocationButton
import com.trm.daylighter.widget.ui.GlanceTheme
import com.trm.daylighter.widget.ui.RetryButton
import com.trm.daylighter.widget.ui.appWidgetBackgroundCornerRadius
import com.trm.daylighter.widget.ui.toPx
import com.trm.daylighter.widget.util.ext.lazyPaint
import java.time.Duration
import java.time.ZonedDateTime

class DefaultLocationChartWidget : GlanceAppWidget() {
  override val stateDefinition = DefaultLocationChartWidgetStateDefinition
  override val sizeMode: SizeMode = SizeMode.Responsive(setOf(wideMode))

  private val nightPaint by lazyPaint(color = nightColor.toArgb())
  private val astronomicalTwilightPaint by lazyPaint(color = astronomicalTwilightColor.toArgb())
  private val nauticalTwilightPaint by lazyPaint(color = nauticalTwilightColor.toArgb())
  private val civilTwilightPaint by lazyPaint(color = civilTwilightColor.toArgb())
  private val dayPaint by lazyPaint(color = dayColor.toArgb())

  @Composable
  override fun Content() {
    GlanceTheme {
      when (val change = currentState<Loadable<LocationSunriseSunsetChange>>()) {
        Empty -> {
          AddLocationButton()
        }
        is Loading -> {
          CircularProgressIndicator()
        }
        is Ready -> {
          DayChart(change = change)
        }
        is Failed -> {
          RetryButton(
            onClick =
              actionSendBroadcast(
                DefaultLocationChartWidgetReceiver.updateIntent(LocalContext.current)
              )
          )
        }
      }
    }
  }

  @Composable
  private fun DayChart(change: Ready<LocationSunriseSunsetChange>) {
    Box(modifier = GlanceModifier.fillMaxSize().appWidgetBackgroundCornerRadius()) {
      Image(
        provider = BitmapImageProvider(dayChartBitmap(change.data)),
        contentDescription = null,
        contentScale = ContentScale.FillBounds,
        modifier = GlanceModifier.fillMaxSize()
      )
    }
  }

  @Composable
  private fun dayChartBitmap(change: LocationSunriseSunsetChange): Bitmap {
    val secondsInDay = (24 * 60 * 60).toFloat()
    val durations = dayPeriodDurationsInSeconds(change.today)
    val paints = dayPeriodPaints()

    val size = LocalSize.current
    val widthPx = size.width.value.toPx
    val heightPx = size.height.value.toPx
    val bitmap = Bitmap.createBitmap(widthPx.toInt(), heightPx.toInt(), Bitmap.Config.ARGB_8888)

    Canvas(bitmap).apply {
      var left = 0f
      var right = left + durations.first() / secondsInDay * widthPx
      durations.indices.forEach { index ->
        drawRect(left, 0f, right, heightPx, paints[index])
        left += durations[index] / secondsInDay * widthPx
        if (index != durations.lastIndex) right += durations[index + 1] / secondsInDay * widthPx
        else right = widthPx
      }
    }

    return bitmap
  }

  private fun dayPeriodDurationsInSeconds(sunriseSunset: SunriseSunset): List<Float> {
    val (
      astronomicalTwilightBegin,
      astronomicalTwilightEnd,
      civilTwilightBegin,
      civilTwilightEnd,
      _,
      nauticalTwilightBegin,
      nauticalTwilightEnd,
      _,
      sunrise,
      sunset,
      date) =
      sunriseSunset
    val periodInstants =
      listOf(
          date.atStartOfDay(sunrise.zone),
          astronomicalTwilightBegin,
          nauticalTwilightBegin,
          civilTwilightBegin,
          sunrise,
          sunset,
          civilTwilightEnd,
          nauticalTwilightEnd,
          astronomicalTwilightEnd,
          date.atStartOfDay(sunrise.zone).plusDays(1L),
        )
        .map(ZonedDateTime::toInstant)
    return periodInstants.indices.drop(1).map { index ->
      Duration.between(periodInstants[index - 1], periodInstants[index]).seconds.toFloat()
    }
  }

  private fun dayPeriodPaints(): List<Paint> =
    listOf(
      nightPaint,
      astronomicalTwilightPaint,
      nauticalTwilightPaint,
      civilTwilightPaint,
      dayPaint,
      civilTwilightPaint,
      nauticalTwilightPaint,
      astronomicalTwilightPaint,
      nightPaint
    )

  companion object {
    private val wideMode = DpSize(200.dp, 50.dp)
  }
}
