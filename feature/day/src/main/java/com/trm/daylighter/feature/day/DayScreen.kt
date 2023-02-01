package com.trm.daylighter.feature.day

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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

@Composable
private fun DayScreen(
  locationSunriseSunsetChange: Loadable<LocationSunriseSunsetChange>,
  onPreviousLocationClick: () -> Unit,
  onNextLocationClick: () -> Unit,
  onAddLocationClick: () -> Unit,
  onRetryClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Box(modifier = modifier) {
    when (locationSunriseSunsetChange) {
      is Empty -> {
        Button(onClick = onAddLocationClick, modifier = Modifier.align(Alignment.Center)) {
          Text(text = "Add location")
        }
      }
      is Ready -> {
        val (location, today, yesterday) = locationSunriseSunsetChange.data

        Canvas(modifier = Modifier.align(Alignment.Center).fillMaxWidth().aspectRatio(1f)) {
          drawArc(
            color = Color.Blue,
            startAngle = 0f,
            sweepAngle = 6f,
            useCenter = true,
            size = size
          )
          drawArc(
            color = Color.Green,
            startAngle = 6f,
            sweepAngle = 6f,
            useCenter = true,
            size = size
          )
          drawArc(
            color = Color.Red,
            startAngle = 12f,
            sweepAngle = 6f,
            useCenter = true,
            size = size
          )
          drawArc(
            color = Color.Black,
            startAngle = 18f,
            sweepAngle = 144f,
            useCenter = true,
            size = size
          )
          drawArc(
            color = Color.Red,
            startAngle = 162f,
            sweepAngle = 6f,
            useCenter = true,
            size = size
          )
          drawArc(
            color = Color.Green,
            startAngle = 168f,
            sweepAngle = 6f,
            useCenter = true,
            size = size
          )
          drawArc(
            color = Color.Blue,
            startAngle = 174f,
            sweepAngle = 6f,
            useCenter = true,
            size = size
          )
          drawArc(
            color = Color.Cyan,
            startAngle = 180f,
            sweepAngle = 180f,
            useCenter = true,
            size = size
          )
        }

        Row(
          modifier = Modifier.align(Alignment.BottomCenter),
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
        Button(onClick = onRetryClick, modifier = Modifier.align(Alignment.Center)) {
          Text("Retry")
        }
      }
      is Loading -> {
        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
      }
    }
  }
}
