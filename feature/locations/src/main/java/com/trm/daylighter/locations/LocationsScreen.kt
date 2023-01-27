package com.trm.daylighter.locations

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.ExperimentalLifecycleComposeApi
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.trm.daylighter.domain.model.*

const val locationsRoute = "locations_route"

@OptIn(ExperimentalLifecycleComposeApi::class)
@Composable
fun LocationsRoute(
  modifier: Modifier = Modifier,
  viewModel: LocationsViewModel = hiltViewModel(),
) {
  val locationsLoadable = viewModel.locations.collectAsStateWithLifecycle(initialValue = Empty)
  LocationsScreen(modifier = modifier, locationsLoadable = locationsLoadable.value)
}

@Composable
private fun LocationsScreen(
  locationsLoadable: Loadable<List<Location>>,
  modifier: Modifier = Modifier
) {
  Box(modifier = modifier) {
    when (locationsLoadable) {
      is WithData -> {
        val locations = locationsLoadable.data
        if (locations.isNotEmpty()) {
          LazyColumn(contentPadding = PaddingValues(10.dp)) {
            items(locations) {
              Row(modifier = Modifier.padding(vertical = 5.dp)) {
                Text(text = "${it.latitude}, ${it.longitude}")
              }
            }
          }
        } else {
          Text(text = "No locations", modifier = Modifier.align(Alignment.Center))
        }
      }
      is WithoutData -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
    }
  }
}
