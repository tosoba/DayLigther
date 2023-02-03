package com.trm.daylighter.feature.day

import android.content.res.Configuration
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.ConstraintLayoutScope
import androidx.constraintlayout.compose.Dimension
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.ExperimentalLifecycleComposeApi
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.trm.daylighter.core.common.util.ext.radians
import com.trm.daylighter.domain.model.*
import com.trm.daylighter.domain.model.LocationSunriseSunsetChange
import kotlin.math.cos
import kotlin.math.sin

const val dayRoute = "day_route"

@OptIn(ExperimentalLifecycleComposeApi::class)
@Composable
fun DayRoute(
  onDrawerMenuClick: () -> Unit,
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
    onDrawerMenuClick = onDrawerMenuClick,
    onPreviousLocationClick = viewModel::previousLocation,
    onNextLocationClick = viewModel::nextLocation,
    onAddLocationClick = onAddLocation,
    onRetryClick = viewModel::retry,
    modifier = modifier
  )
}

data class DayChartSegment(
  val sweepAngleDegrees: Float,
  val color: Color,
  val periodLabel: String,
  val endingEdgeLabel: String
)

@Composable
private fun DayScreen(
  locationSunriseSunsetChange: Loadable<LocationSunriseSunsetChange>,
  onDrawerMenuClick: () -> Unit,
  onPreviousLocationClick: () -> Unit,
  onNextLocationClick: () -> Unit,
  onAddLocationClick: () -> Unit,
  onRetryClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  ConstraintLayout(modifier = modifier) {
    when (locationSunriseSunsetChange) {
      is Empty -> {
        val (drawerMenuButton, addLocationButton) = createRefs()
        DrawerMenuButton(
          onDrawerMenuClick = onDrawerMenuClick,
          modifier =
            Modifier.constrainAs(drawerMenuButton) {
              start.linkTo(parent.start, 16.dp)
              top.linkTo(parent.top, 16.dp)
            }
        )
        Button(
          onClick = onAddLocationClick,
          modifier =
            Modifier.constrainAs(addLocationButton) {
              linkTo(parent.start, parent.end)
              linkTo(parent.top, parent.bottom)
            }
        ) {
          Text(text = "Add location")
        }
      }
      is Ready -> {
        SunriseSunset(
          locationSunriseSunsetChange = locationSunriseSunsetChange.data,
          onDrawerMenuClick = onDrawerMenuClick,
          onPreviousLocationClick = onPreviousLocationClick,
          onNextLocationClick = onNextLocationClick
        )
      }
      is Failed -> {
        val (drawerMenuButton, retryButton) = createRefs()
        DrawerMenuButton(
          onDrawerMenuClick = onDrawerMenuClick,
          modifier =
            Modifier.constrainAs(drawerMenuButton) {
              start.linkTo(parent.start, 16.dp)
              top.linkTo(parent.top, 16.dp)
            }
        )
        Button(
          onClick = onRetryClick,
          modifier =
            Modifier.constrainAs(retryButton) {
              linkTo(parent.start, parent.end)
              linkTo(parent.top, parent.bottom)
            }
        ) {
          Text("Retry")
        }
      }
      is Loading -> {
        val (drawerMenuButton, loadingIndicator) = createRefs()
        DrawerMenuButton(
          onDrawerMenuClick = onDrawerMenuClick,
          modifier =
            Modifier.constrainAs(drawerMenuButton) {
              start.linkTo(parent.start, 16.dp)
              top.linkTo(parent.top, 16.dp)
            }
        )

        CircularProgressIndicator(
          modifier =
            Modifier.constrainAs(loadingIndicator) {
              linkTo(parent.start, parent.end)
              linkTo(parent.top, parent.bottom)
            }
        )
      }
    }
  }
}

@Composable
private fun DrawerMenuButton(onDrawerMenuClick: () -> Unit, modifier: Modifier = Modifier) {
  SmallFloatingActionButton(onClick = onDrawerMenuClick, modifier = modifier) {
    Icon(imageVector = Icons.Filled.Menu, contentDescription = "drawer_menu")
  }
}

@Composable
private fun ConstraintLayoutScope.SunriseSunset(
  locationSunriseSunsetChange: LocationSunriseSunsetChange,
  onDrawerMenuClick: () -> Unit,
  onPreviousLocationClick: () -> Unit,
  onNextLocationClick: () -> Unit,
) {
  val (drawerMenuButton, chart, navigation, prevLocationButton, nextLocationButton) = createRefs()
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
    DrawerMenuButton(
      onDrawerMenuClick = onDrawerMenuClick,
      modifier =
        Modifier.constrainAs(drawerMenuButton) {
          start.linkTo(parent.start, 16.dp)
          top.linkTo(parent.top, 16.dp)
        }
    )

    PrevLocationButton(
      onClick = onPreviousLocationClick,
      modifier =
        Modifier.constrainAs(prevLocationButton) {
          bottom.linkTo(navigation.top, 16.dp)
          start.linkTo(parent.start, 16.dp)
        }
    )

    SunriseSunsetNavigationBar(
      modifier =
        Modifier.constrainAs(navigation) {
          linkTo(chart.bottom, parent.bottom)
          linkTo(parent.start, parent.end)
        }
    )

    NextLocationButton(
      onClick = onNextLocationClick,
      Modifier.constrainAs(nextLocationButton) {
        bottom.linkTo(navigation.top, 16.dp)
        end.linkTo(parent.end, 16.dp)
      }
    )
  } else {
    PrevLocationButton(
      onClick = onPreviousLocationClick,
      modifier =
        Modifier.constrainAs(prevLocationButton) {
          bottom.linkTo(parent.bottom, 16.dp)
          start.linkTo(navigation.end, 16.dp)
        }
    )

    SunriseSunsetNavigationRail(
      onDrawerMenuClick = onDrawerMenuClick,
      modifier =
        Modifier.constrainAs(navigation) {
          linkTo(parent.start, chart.start)
          linkTo(parent.top, parent.bottom)
        }
    )

    NextLocationButton(
      onClick = onNextLocationClick,
      Modifier.constrainAs(nextLocationButton) {
        bottom.linkTo(parent.bottom, 16.dp)
        end.linkTo(parent.end, 16.dp)
      }
    )
  }
}

@Composable
private fun PrevLocationButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
  SmallFloatingActionButton(modifier = modifier, onClick = onClick) {
    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "prev_location")
  }
}

@Composable
private fun NextLocationButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
  SmallFloatingActionButton(modifier = modifier, onClick = onClick) {
    Icon(imageVector = Icons.Default.ArrowForward, contentDescription = "next_location")
  }
}

@Composable
private fun SunriseSunsetNavigationBar(modifier: Modifier = Modifier) {
  NavigationBar(modifier = modifier) {
    NavigationBarItem(
      selected = true,
      onClick = {},
      icon = {
        Icon(painter = painterResource(R.drawable.sunrise), contentDescription = "sunrise")
      },
      label = { Text(text = stringResource(R.string.sunrise)) }
    )
    NavigationBarItem(
      selected = false,
      onClick = {},
      icon = { Icon(painter = painterResource(R.drawable.sunset), contentDescription = "sunset") },
      label = { Text(text = stringResource(R.string.sunset)) }
    )
  }
}

@Composable
private fun SunriseSunsetNavigationRail(
  onDrawerMenuClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  NavigationRail(
    header = {
      DrawerMenuButton(
        onDrawerMenuClick = onDrawerMenuClick,
        modifier = Modifier.padding(top = 8.dp)
      )
    },
    modifier = modifier
  ) {
    Spacer(modifier = Modifier.weight(1f))
    NavigationRailItem(
      selected = true,
      onClick = {},
      icon = {
        Icon(painter = painterResource(R.drawable.sunrise), contentDescription = "sunrise")
      },
      label = { Text(text = stringResource(R.string.sunrise)) }
    )
    NavigationRailItem(
      selected = false,
      onClick = {},
      icon = { Icon(painter = painterResource(R.drawable.sunset), contentDescription = "sunset") },
      label = { Text(text = stringResource(R.string.sunset)) }
    )
    Spacer(modifier = Modifier.weight(1f))
  }
}

@OptIn(ExperimentalTextApi::class)
@Composable
private fun SunriseSunsetChart(modifier: Modifier) {
  val chartSegments = remember {
    listOf(
      DayChartSegment(
        sweepAngleDegrees = 90f,
        color = Color.Cyan,
        periodLabel = "Day",
        endingEdgeLabel = "Sunrise"
      ),
      DayChartSegment(
        sweepAngleDegrees = 6f,
        color = Color.Blue,
        periodLabel = "Civil twilight",
        endingEdgeLabel = "Civil dawn - 6º below"
      ),
      DayChartSegment(
        sweepAngleDegrees = 6f,
        color = Color.Green,
        periodLabel = "Nautical twilight",
        endingEdgeLabel = "Nautical dawn - 12º below"
      ),
      DayChartSegment(
        sweepAngleDegrees = 6f,
        color = Color.Red,
        periodLabel = "Astronomical twilight",
        endingEdgeLabel = "Astronomical dawn - 18º below"
      ),
      DayChartSegment(
        sweepAngleDegrees = 72f,
        color = Color.Black,
        periodLabel = "Night",
        endingEdgeLabel = ""
      ),
    )
  }

  val orientation = LocalConfiguration.current.orientation
  val textMeasurer = rememberTextMeasurer()
  val labelSmallTextStyle = MaterialTheme.typography.labelSmall

  Canvas(modifier = modifier) {
    val topLeftOffset =
      Offset(
        -size.height * if (orientation == Configuration.ORIENTATION_PORTRAIT) 1.7f else 1f,
        -size.height * .5f
      )
    val segmentSize = Size(size.height, size.height) * 2f
    var startAngle = -90f

    fun DrawScope.drawChartSegment(segment: DayChartSegment) {
      drawArc(
        color = segment.color,
        startAngle = startAngle,
        sweepAngle = segment.sweepAngleDegrees,
        useCenter = true,
        topLeft = topLeftOffset,
        size = segmentSize
      )
    }

    chartSegments.forEach { segment ->
      drawChartSegment(segment)
      startAngle += segment.sweepAngleDegrees
    }

    val chartRadius = segmentSize.maxDimension / 2f
    val chartCenter = Offset(topLeftOffset.x + chartRadius, size.height / 2f)
    var currentAngleDegrees = 0f
    val angleIncrementDegrees = 6f
    val textPadding = 3.dp.toPx()

    repeat(chartSegments.size - 1) { segmentIndex ->
      val lineRadiusMultiplier = if (segmentIndex == 0) 10f else 1.1f
      drawLine(
        color = chartSegments[segmentIndex].color,
        start = chartCenter,
        end =
          Offset(
            chartCenter.x + chartRadius * lineRadiusMultiplier * cos(currentAngleDegrees.radians),
            chartCenter.y + chartRadius * lineRadiusMultiplier * sin(currentAngleDegrees.radians)
          ),
      )

      val textRadiusMultiplier = 1.1f
      rotate(degrees = currentAngleDegrees, pivot = chartCenter) {
        val label = chartSegments[segmentIndex].endingEdgeLabel
        val textLayoutResult = textMeasurer.measure(text = AnnotatedString(label))
        drawText(
          textMeasurer = textMeasurer,
          text = label,
          topLeft =
            Offset(
              x = chartCenter.x + chartRadius * textRadiusMultiplier + textPadding,
              y =
                chartCenter.y + textPadding -
                  if (segmentIndex == 0) 0f else textLayoutResult.lastBaseline
            ),
        )
      }

      currentAngleDegrees += angleIncrementDegrees
    }

    currentAngleDegrees = 0f
    chartSegments.forEachIndexed { index, segment ->
      rotate(degrees = currentAngleDegrees, pivot = chartCenter) {
        val textLayoutResult = textMeasurer.measure(text = AnnotatedString(segment.periodLabel))
        drawText(
          textMeasurer = textMeasurer,
          text = segment.periodLabel,
          topLeft =
            Offset(
              x = chartCenter.x + chartRadius - textLayoutResult.size.width - textPadding,
              y = chartCenter.y - textLayoutResult.size.height - textPadding
            ),
          style =
            labelSmallTextStyle.copy(
              color = if (index == 0) Color.Black else Color.White,
              textAlign = TextAlign.Right
            ),
        )
        currentAngleDegrees += angleIncrementDegrees
      }
    }
  }
}
