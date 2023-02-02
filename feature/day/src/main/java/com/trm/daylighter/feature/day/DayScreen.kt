package com.trm.daylighter.feature.day

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
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
        val (canvas, controls) = createRefs()

        val (location, today, yesterday) = locationSunriseSunsetChange.data
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

        Canvas(
          modifier =
            Modifier.constrainAs(canvas) {
              linkTo(parent.start, parent.end)
              linkTo(parent.top, controls.top, topMargin = 20.dp, bottomMargin = 20.dp)
              height = Dimension.fillToConstraints
              width = Dimension.fillToConstraints
            }
        ) {
          var startAngle = 0f
          chartSegments.forEach { (sweepAngleDegrees, color) ->
            drawArc(
              color = color,
              startAngle = startAngle,
              sweepAngle = sweepAngleDegrees,
              useCenter = true,
              topLeft = Offset(-size.height * 1.5f, -size.height * 0.5f),
              size = Size(size.height, size.height) * 2f
            )
            startAngle += sweepAngleDegrees
          }
        }

        Row(
          modifier =
            Modifier.constrainAs(controls) {
                linkTo(parent.start, parent.end)
                linkTo(canvas.bottom, parent.bottom)
              }
              .background(Color.LightGray),
          verticalAlignment = Alignment.CenterVertically
        ) {
          IconButton(onClick = onPreviousLocationClick) {
            Icon(imageVector = Icons.Filled.SkipPrevious, contentDescription = "previous_location")
          }
          IconButton(onClick = onNextLocationClick) {
            Icon(imageVector = Icons.Filled.SkipNext, contentDescription = "next_location")
          }
        }
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
