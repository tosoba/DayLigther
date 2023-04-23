package com.trm.daylighter.widget.locations

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.currentState
import com.trm.daylighter.core.domain.model.Empty
import com.trm.daylighter.core.domain.model.Loadable
import com.trm.daylighter.core.domain.model.LocationSunriseSunsetChange
import com.trm.daylighter.widget.ui.AddLocationButton
import com.trm.daylighter.widget.ui.GlanceTheme

class LocationsSunriseSunsetWidget : GlanceAppWidget() {
  override val stateDefinition = LocationsSunriseSunsetWidgetStateDefinition
  override val sizeMode: SizeMode = SizeMode.Responsive(setOf(smallMode, wideMode, squareMode))

  @Composable
  override fun Content() {
    GlanceTheme {
      when (val changes = currentState<Loadable<List<LocationSunriseSunsetChange>>>()) {
        Empty -> AddLocationButton()
        else -> AddLocationButton() // TODO:
      }
    }
  }

  companion object {
    private val smallMode = DpSize(120.dp, 50.dp)
    private val wideMode = DpSize(200.dp, 50.dp)
    private val squareMode = DpSize(120.dp, 120.dp)
  }
}