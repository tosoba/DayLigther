package com.trm.daylighter.feature.location

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trm.daylighter.core.common.util.MapDefaults
import com.trm.daylighter.core.domain.model.*
import com.trm.daylighter.core.domain.usecase.GetCurrentUserLatLngUseCase
import com.trm.daylighter.core.domain.usecase.GetLocationById
import com.trm.daylighter.core.domain.usecase.SaveLocationUseCase
import com.trm.daylighter.feature.location.exception.UserLatLngNotFound
import com.trm.daylighter.feature.location.model.MapPosition
import com.trm.daylighter.feature.location.model.SaveLocationType
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@HiltViewModel
class LocationViewModel
@Inject
constructor(
  private val savedStateHandle: SavedStateHandle,
  private val getLocationById: GetLocationById,
  private val saveLocationUseCase: SaveLocationUseCase,
  private val getCurrentUserLatLngUseCase: GetCurrentUserLatLngUseCase,
) : ViewModel() {
  private val _saveLocationTypeFlow = MutableSharedFlow<SaveLocationType>()
  private val savingFlow: SharedFlow<Loadable<Unit>> =
    _saveLocationTypeFlow
      .transformLatest { saveLocationType ->
        when (saveLocationType) {
          is SaveLocationType.Specified ->
            emitSaveSpecifiedLocation(saveLocationType.latitude, saveLocationType.longitude)
          is SaveLocationType.User -> emitSaveUserLocation()
          is SaveLocationType.CanceCurrent -> emit(Empty)
        }
      }
      .shareIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(5000L), replay = 1)

  val savedFlow: Flow<Unit> = savingFlow.filterIsInstance<Ready<Unit>>().map { it.data }
  val loadingFlow: Flow<Boolean> = savingFlow.map { it is Loading }
  val userLocationNotFoundFlow: Flow<Boolean> =
    savingFlow.map { it.isFailedWith<UserLatLngNotFound>() }

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
      .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = MapPosition()
      )

  fun saveSpecifiedLocation(latitude: Double, longitude: Double) {
    viewModelScope.launch {
      _saveLocationTypeFlow.emit(
        SaveLocationType.Specified(latitude = latitude, longitude = longitude)
      )
    }
  }

  fun getAndSaveUserLocation() {
    viewModelScope.launch { _saveLocationTypeFlow.emit(SaveLocationType.User) }
  }

  fun cancelSaveLocation() {
    viewModelScope.launch { _saveLocationTypeFlow.emit(SaveLocationType.CanceCurrent) }
  }

  private suspend fun FlowCollector<Loadable<Unit>>.emitSaveSpecifiedLocation(
    latitude: Double,
    longitude: Double
  ) {
    emit(LoadingFirst)
    saveLocation(latitude = latitude, longitude = longitude)
    emit(Ready(Unit))
  }

  private suspend fun FlowCollector<Loadable<Unit>>.emitSaveUserLocation() {
    emit(LoadingFirst)
    val userLatLng = getCurrentUserLatLngUseCase()
    if (userLatLng == null) {
      emit(FailedFirst(UserLatLngNotFound))
      delay(3_500L)
      emit(Empty)
    } else {
      saveLocation(latitude = userLatLng.latitude, longitude = userLatLng.longitude)
      emit(Ready(Unit))
    }
  }

  private suspend fun saveLocation(latitude: Double, longitude: Double) {
    val locationId = savedStateHandle.get<Long>(locationIdParam)
    if (locationId == null) {
      saveLocationUseCase(latitude = latitude, longitude = longitude)
    } else {
      saveLocationUseCase(id = locationId, latitude = latitude, longitude = longitude)
    }
  }
}
