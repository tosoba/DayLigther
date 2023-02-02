package com.trm.daylighter.feature.day

import android.content.res.Configuration
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.ConstraintLayoutScope
import androidx.constraintlayout.compose.Dimension
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.ExperimentalLifecycleComposeApi
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.trm.daylighter.domain.model.*
import com.trm.daylighter.domain.model.LocationSunriseSunsetChange

const val dayRoute = "day_route"

@OptIn(ExperimentalLifecycleComposeApi::class)
@Composable
fun DayRoute(
  onAddLocation: () -> Unit,
  modifier: Modifier = Modifier,
  viewModel: DayViewModel = hiltViewModel(),
) {
  val locationSunriseSunsetChange =
    viewModel.currentLocationSunriseSunsetChange.collectAsStateWithLifecycle(
      initialValue = LoadingFirst
    )
  DayScreen(
    locationSunriseSunsetChange = locationSunriseSunsetChange.value,
    onPreviousLocationClick = viewModel::previousLocation,
    onNextLocationClick = viewModel::nextLocation,
    onAddLocationClick = onAddLocation,
    onRetryClick = viewModel::retry,
    modifier = modifier
  )
}

data class DayChartSegment(val sweepAngleDegrees: Float, val color: Color)

@Composable
private fun DayScreen(
  locationSunriseSunsetChange: Loadable<LocationSunriseSunsetChange>,
  onPreviousLocationClick: () -> Unit,
  onNextLocationClick: () -> Unit,
  onAddLocationClick: () -> Unit,
  onRetryClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  ConstraintLayout(modifier = modifier) {
    when (locationSunriseSunsetChange) {
      is Empty -> {
        val button = createRef()
        Button(
          onClick = onAddLocationClick,
          modifier =
            Modifier.constrainAs(button) {
              linkTo(parent.start, parent.end)
              linkTo(parent.top, parent.bottom)
            }
        ) {
          Text(text = "Add location")
        }
      }
      is Ready -> {
        SunriseSunset(locationSunriseSunsetChange = locationSunriseSunsetChange.data)
      }
      is Failed -> {
        val button = createRef()
        Button(
          onClick = onRetryClick,
          modifier =
            Modifier.constrainAs(button) {
              linkTo(parent.start, parent.end)
              linkTo(parent.top, parent.bottom)
            }
        ) {
          Text("Retry")
        }
      }
      is Loading -> {
        val indicator = createRef()
        CircularProgressIndicator(
          modifier =
            Modifier.constrainAs(indicator) {
              linkTo(parent.start, parent.end)
              linkTo(parent.top, parent.bottom)
            }
        )
      }
    }
  }
}

@Composable
private fun ConstraintLayoutScope.SunriseSunset(
  locationSunriseSunsetChange: LocationSunriseSunsetChange
) {
  val (chart, navigation) = createRefs()
  val (location, today, yesterday) = locationSunriseSunsetChange
  val orientation = LocalConfiguration.current.orientation

  SunriseSunsetChart(
    modifier =
      Modifier.constrainAs(chart) {
        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
          linkTo(parent.start, parent.end)
          linkTo(parent.top, navigation.top)
        } else {
          linkTo(navigation.end, parent.end)
          linkTo(parent.top, parent.bottom)
        }
        height = Dimension.fillToConstraints
        width = Dimension.fillToConstraints
      }
  )

  if (orientation == Configuration.ORIENTATION_PORTRAIT) {
    NavigationBar(
      modifier =
        Modifier.constrainAs(navigation) {
          linkTo(chart.bottom, parent.bottom)
          linkTo(parent.start, parent.end)
        }
    ) {
      NavigationBarItem(
        selected = true,
        onClick = {},
        icon = {
          Icon(painter = painterResource(R.drawable.sunrise), contentDescription = "sunrise")
        },
        label = { Text(text = "Sunrise") }
      )
      NavigationBarItem(
        selected = false,
        onClick = {},
        icon = {
          Icon(painter = painterResource(R.drawable.sunset), contentDescription = "sunset")
        },
        label = { Text(text = "Sunset") }
      )
    }
  } else {
    NavigationRail(
      modifier =
        Modifier.constrainAs(navigation) {
          linkTo(parent.start, chart.start)
          linkTo(parent.top, parent.bottom)
        }
    ) {
      Spacer(modifier = Modifier.weight(1f))
      NavigationRailItem(
        selected = true,
        onClick = {},
        icon = {
          Icon(painter = painterResource(R.drawable.sunrise), contentDescription = "sunrise")
        },
        label = { Text(text = "Sunrise") }
      )
      NavigationRailItem(
        selected = false,
        onClick = {},
        icon = {
          Icon(painter = painterResource(R.drawable.sunset), contentDescription = "sunset")
        },
        label = { Text(text = "Sunset") }
      )
      Spacer(modifier = Modifier.weight(1f))
    }
  }
}

@Composable
private fun SunriseSunsetChart(modifier: Modifier) {
  val chartSegments = remember {
    sequenceOf(
      DayChartSegment(sweepAngleDegrees = 6f, color = Color.Blue),
      DayChartSegment(sweepAngleDegrees = 6f, color = Color.Green),
      DayChartSegment(sweepAngleDegrees = 6f, color = Color.Red),
      DayChartSegment(sweepAngleDegrees = 144f, color = Color.Black),
      DayChartSegment(sweepAngleDegrees = 6f, color = Color.Red),
      DayChartSegment(sweepAngleDegrees = 6f, color = Color.Green),
      DayChartSegment(sweepAngleDegrees = 6f, color = Color.Blue),
      DayChartSegment(sweepAngleDegrees = 180f, color = Color.Cyan),
    )
  }

  val orientation = LocalConfiguration.current.orientation

  fun DrawScope.segmentTopLeftOffset(): Offset =
    if (orientation == Configuration.ORIENTATION_PORTRAIT) {
      Offset(-size.height * 1.65f, -size.height * 0.5f)
    } else {
      Offset(-size.height * 2f, -size.height * 1f)
    }

  fun DrawScope.segmentSize(): Size =
    if (orientation == Configuration.ORIENTATION_PORTRAIT) Size(size.height, size.height) * 2f
    else Size(size.height, size.height) * 3f

  Canvas(modifier = modifier) {
    val topLeftOffset = segmentTopLeftOffset()
    val size = segmentSize()
    var startAngle = 0f
    chartSegments.forEach { (sweepAngleDegrees, color) ->
      drawArc(
        color = color,
        startAngle = startAngle,
        sweepAngle = sweepAngleDegrees,
        useCenter = true,
        topLeft = topLeftOffset,
        size = size
      )
      startAngle += sweepAngleDegrees
    }
  }
}
