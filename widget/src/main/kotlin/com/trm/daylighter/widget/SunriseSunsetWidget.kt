package com.trm.daylighter.widget

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.glance.appwidget.CircularProgressIndicator
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.text.Text
import com.trm.daylighter.core.domain.model.*
import com.trm.daylighter.core.ui.theme.DayLighterTheme
import com.trm.daylighter.widget.util.AppWidgetBox

class SunriseSunsetWidget : GlanceAppWidget() {
  override val stateDefinition = SunriseSunsetWidgetStateDefinition

  override val sizeMode: SizeMode =
    SizeMode.Responsive(setOf(thinMode, smallMode, mediumMode, largeMode))

  @Composable
  override fun Content() {
    val location = currentState<Loadable<Location>>()
    DayLighterTheme {
      AppWidgetBox(contentAlignment = Alignment.Center) {
        when (location) {
          is Failed -> Text(text = "Failed")
          is Loading -> CircularProgressIndicator()
          is Ready -> Text(text = location.data.id.toString())
          is Empty -> Text(text = "Empty")
        }
      }
    }
  }

  companion object {
    private val thinMode = DpSize(120.dp, 120.dp)
    private val smallMode = DpSize(184.dp, 184.dp)
    private val mediumMode = DpSize(260.dp, 200.dp)
    private val largeMode = DpSize(260.dp, 280.dp)
  }
}
