package com.trm.daylighter.feature.location

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trm.daylighter.core.common.util.ext.MapDefaults
import com.trm.daylighter.core.domain.usecase.GetLocationById
import com.trm.daylighter.core.domain.usecase.SaveLocationUseCase
import com.trm.daylighter.feature.location.model.MapPosition
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@HiltViewModel
class LocationViewModel
@Inject
constructor(
  private val savedStateHandle: SavedStateHandle,
  private val getLocationById: GetLocationById,
  private val saveLocationUseCase: SaveLocationUseCase,
) : ViewModel() {
  private val _savedFlow = MutableSharedFlow<Unit>(replay = 1)
  val savedFlow = _savedFlow.asSharedFlow()

  val initialMapPositionFlow: StateFlow<MapPosition> =
    flow {
        savedStateHandle.get<Long>(locationIdParam)?.let {
          val location = getLocationById(id = it)
          emit(
            MapPosition(
              latitude = location.latitude,
              longitude = location.longitude,
              zoom = MapDefaults.INITIAL_LOCATION_ZOOM
            )
          )
        }
      }
      .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), MapPosition())

  fun saveLocation(latitude: Double, longitude: Double) {
    viewModelScope.launch {
      val locationId = savedStateHandle.get<Long>(locationIdParam)
      if (locationId == null) {
        saveLocationUseCase(latitude = latitude, longitude = longitude)
      } else {
        // TODO:
      }
      _savedFlow.emit(Unit)
    }
  }
}
