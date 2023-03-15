package com.trm.daylighter.feature.location

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trm.daylighter.core.common.util.MapDefaults
import com.trm.daylighter.core.domain.model.Loadable
import com.trm.daylighter.core.domain.model.Loading
import com.trm.daylighter.core.domain.model.LoadingFirst
import com.trm.daylighter.core.domain.model.Ready
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
  private val _savingFlow = MutableSharedFlow<Loadable<Unit>>(replay = 1)
  val savedFlow: Flow<Unit> =
    _savingFlow.asSharedFlow().filterIsInstance<Ready<Unit>>().map { it.data }
  val isLoadingFlow: Flow<Boolean> = _savingFlow.asSharedFlow().map { it is Loading }

  val initialMapPositionFlow: StateFlow<MapPosition> =
    flow {
        savedStateHandle.get<Long>(locationIdParam)?.let { id ->
          val location = getLocationById(id)
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
      _savingFlow.emit(LoadingFirst)
      val locationId = savedStateHandle.get<Long>(locationIdParam)
      if (locationId == null) {
        saveLocationUseCase(latitude = latitude, longitude = longitude)
      } else {
        saveLocationUseCase(id = locationId, latitude = latitude, longitude = longitude)
      }
      _savingFlow.emit(Ready(Unit))
    }
  }
}
