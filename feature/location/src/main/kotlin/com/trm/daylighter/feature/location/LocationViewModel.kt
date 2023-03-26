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
import com.trm.daylighter.feature.location.model.LocationScreenMode
import com.trm.daylighter.feature.location.model.MapPosition
import com.trm.daylighter.feature.location.model.SaveLocationProcess
import com.trm.daylighter.feature.location.model.SaveLocationRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@HiltViewModel
class LocationViewModel
@Inject
constructor(
  savedStateHandle: SavedStateHandle,
  private val getLocationById: GetLocationById,
  private val saveLocationUseCase: SaveLocationUseCase,
  private val getCurrentUserLatLngUseCase: GetCurrentUserLatLngUseCase,
) : ViewModel() {
  private val _saveLocationRequestFlow = MutableSharedFlow<SaveLocationRequest>()
  private val saveLocationProcessFlow: SharedFlow<Loadable<SaveLocationProcess>> =
    _saveLocationRequestFlow
      .transformLatest { saveLocationType ->
        when (saveLocationType) {
          is SaveLocationRequest.Specified -> {
            emitSpecifiedLocation(saveLocationType.latitude, saveLocationType.longitude)
          }
          is SaveLocationRequest.User -> {
            emitUserLocation()
          }
          is SaveLocationRequest.CancelCurrent -> {
            emit(Empty)
          }
        }
      }
      .shareIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(5000L), replay = 1)

  val loadingFlow: Flow<Boolean> = saveLocationProcessFlow.map { it is Loading }
  val userLocationNotFoundFlow: Flow<Boolean> =
    saveLocationProcessFlow.map { it.isFailedWith<UserLatLngNotFound>() }
  val currentSaveLocationProcessFlow: Flow<SaveLocationProcess?> =
    saveLocationProcessFlow.map { if (it is Ready) it.data else null }

  private val _locationSavedFlow = MutableSharedFlow<Unit>(replay = 1)
  val locationSavedFlow = _locationSavedFlow.asSharedFlow()

  private val initialLocationId: Long? = savedStateHandle.get<Long>(locationIdParam)

  val initialMapPositionFlow: StateFlow<MapPosition> =
    flow {
        initialLocationId?.let { id ->
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

  val screenMode: LocationScreenMode =
    if (initialLocationId == null) LocationScreenMode.ADD else LocationScreenMode.EDIT

  fun onSaveSpecifiedLocationRequest(latitude: Double, longitude: Double) {
    viewModelScope.launch {
      _saveLocationRequestFlow.emit(
        SaveLocationRequest.Specified(latitude = latitude, longitude = longitude)
      )
    }
  }

  fun onSaveUserLocationRequest() {
    viewModelScope.launch { _saveLocationRequestFlow.emit(SaveLocationRequest.User) }
  }

  fun onCancelCurrentSaveLocation() {
    viewModelScope.launch { _saveLocationRequestFlow.emit(SaveLocationRequest.CancelCurrent) }
  }

  fun saveLocation(latitude: Double, longitude: Double, name: String) {
    viewModelScope.launch {
      if (initialLocationId == null) {
        saveLocationUseCase(latitude = latitude, longitude = longitude)
      } else {
        saveLocationUseCase(id = initialLocationId, latitude = latitude, longitude = longitude)
      }
      _locationSavedFlow.emit(Unit)
    }
  }

  private suspend fun FlowCollector<Loadable<SaveLocationProcess>>.emitSpecifiedLocation(
    latitude: Double,
    longitude: Double
  ) {
    emit(LoadingFirst)
    emit(
      SaveLocationProcess(latitude = latitude, longitude = longitude, isUser = false).asLoadable()
    )
  }

  private suspend fun FlowCollector<Loadable<SaveLocationProcess>>.emitUserLocation() {
    emit(LoadingFirst)
    val userLatLng = getCurrentUserLatLngUseCase()
    if (userLatLng == null) {
      emit(FailedFirst(UserLatLngNotFound))
      delay(3_500L)
      emit(Empty)
    } else {
      emit(
        SaveLocationProcess(
            latitude = userLatLng.latitude,
            longitude = userLatLng.longitude,
            isUser = true
          )
          .asLoadable()
      )
    }
  }
}
