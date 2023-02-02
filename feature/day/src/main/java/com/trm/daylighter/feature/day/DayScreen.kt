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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
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

data class DayChartSegment(val sweepAngleDegrees: Float, val color: Color)

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

    NavigationRail(
      header = {
        DrawerMenuButton(
          onDrawerMenuClick = onDrawerMenuClick,
          modifier = Modifier.padding(top = 8.dp)
        )
      },
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
