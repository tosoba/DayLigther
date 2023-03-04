package com.trm.daylighter.widget

import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.glance.Button
import androidx.glance.appwidget.CircularProgressIndicator
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.text.Text
import com.trm.daylighter.core.common.R as commonR
import com.trm.daylighter.core.domain.model.*
import com.trm.daylighter.core.ui.theme.DayLighterTheme
import com.trm.daylighter.widget.util.AppWidgetBox
import com.trm.daylighter.widget.util.stringResource

class DefaultLocationSunriseSunsetWidget : GlanceAppWidget() {
  override val stateDefinition = DefaultLocationSunriseSunsetWidgetStateDefinition

  override val sizeMode: SizeMode =
    SizeMode.Responsive(setOf(thinMode, smallMode, mediumMode, largeMode))

  @Composable
  override fun Content() {
    val loadable = currentState<Loadable<LocationSunriseSunsetChange>>()
    DayLighterTheme(darkTheme = false, tweakStatusBarAppearance = false) {
      AppWidgetBox(contentAlignment = Alignment.Center) {
        when (loadable) {
          is Failed -> Text(text = "Failed")
          is Loading -> CircularProgressIndicator()
          is Ready -> Text(text = loadable.data.location.id.toString())
          is Empty -> AddLocationButton()
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

@Composable
private fun AddLocationButton() {
  Button(
    text = stringResource(id = commonR.string.add_location),
    onClick =
      actionStartActivity(
        Intent(
          Intent.ACTION_VIEW,
          stringResource(commonR.string.add_location_deep_link_uri).toUri()
        )
      )
  )
}
