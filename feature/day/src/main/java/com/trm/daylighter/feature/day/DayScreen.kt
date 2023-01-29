package com.trm.daylighter.feature.day

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
  val locationSunriseSunsetChangeLoadable =
    viewModel.currentLocationSunriseSunsetChange.collectAsStateWithLifecycle(
      initialValue = LoadingFirst
    )
  DayScreen(
    locationSunriseSunsetChangeLoadable = locationSunriseSunsetChangeLoadable.value,
    onPreviousLocationClick = viewModel::previousLocation,
    onNextLocationClick = viewModel::nextLocation,
    onAddLocationClick = onAddLocation,
    modifier = modifier
  )
}

@Composable
private fun DayScreen(
  locationSunriseSunsetChangeLoadable: Loadable<LocationSunriseSunsetChange>,
  onPreviousLocationClick: () -> Unit,
  onNextLocationClick: () -> Unit,
  onAddLocationClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  when (locationSunriseSunsetChangeLoadable) {
    is Empty -> {
      Box(modifier = modifier) {
        Button(onClick = onAddLocationClick, modifier = Modifier.align(Alignment.Center)) {
          Text(text = "Add location")
        }
      }
    }
    is Ready -> {
      val (location, today, yesterday) = locationSunriseSunsetChangeLoadable.data
      Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
      ) {
        IconButton(onClick = onPreviousLocationClick) {
          Icon(imageVector = Icons.Filled.SkipPrevious, contentDescription = "previous_location")
        }
        Column {
          Text(
            text = """${location.latitude}, ${location.longitude}""",
          )
          Text(text = "${yesterday.dayLengthSeconds} -> ${today.dayLengthSeconds}")
        }
        IconButton(onClick = onNextLocationClick) {
          Icon(imageVector = Icons.Filled.SkipNext, contentDescription = "next_location")
        }
      }
    }
    is Failed -> {
      Button(onClick = {}) { Text("Retry") }
    }
    is Loading -> {
      Box(modifier = modifier) {
        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
      }
    }
  }
}
