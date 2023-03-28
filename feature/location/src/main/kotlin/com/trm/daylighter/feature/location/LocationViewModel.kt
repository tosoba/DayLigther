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
import com.trm.daylighter.feature.location.model.LocationPreparedToSave
import com.trm.daylighter.feature.location.model.LocationScreenMode
import com.trm.daylighter.feature.location.model.MapPosition
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
  private val saveLocationRequestFlow = MutableSharedFlow<SaveLocationRequest>()
  private val prepareSaveLocationFlow: SharedFlow<Loadable<LocationPreparedToSave>> =
    saveLocationRequestFlow
      .transformLatest { saveLocationType ->
        when (saveLocationType) {
          is SaveLocationRequest.Specified -> {
            emitSpecifiedLocation(
              latitude = saveLocationType.latitude,
              longitude = saveLocationType.longitude
            )
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

  val userLocationNotFoundFlow: Flow<Boolean> =
    prepareSaveLocationFlow.map { it.isFailedWith<UserLatLngNotFound>() }
  val locationPreparedToSaveFlow: Flow<LocationPreparedToSave?> =
    prepareSaveLocationFlow.map { if (it is Ready) it.data else null }

  private val saveLocationFlow: MutableSharedFlow<Loadable<Unit>> = MutableSharedFlow(replay = 1)
  val locationSavedFlow: Flow<Ready<Unit>> = saveLocationFlow.filterIsInstance()

  val loadingFlow: Flow<Boolean> =
    combine(
      prepareSaveLocationFlow.map { it is Loading }.onStart { emit(false) },
      saveLocationFlow.map { it is Loading }.onStart { emit(false) }
    ) { isLocationBeingPrepared, isLocationBeingSaved ->
      isLocationBeingPrepared || isLocationBeingSaved
    }

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

  fun requestSaveSpecifiedLocation(latitude: Double, longitude: Double) {
    viewModelScope.launch {
      saveLocationRequestFlow.emit(
        SaveLocationRequest.Specified(latitude = latitude, longitude = longitude)
      )
    }
  }

  fun requestGetAndSaveUserLocation() {
    viewModelScope.launch { saveLocationRequestFlow.emit(SaveLocationRequest.User) }
  }

  fun cancelCurrentSaveLocationRequest() {
    viewModelScope.launch { saveLocationRequestFlow.emit(SaveLocationRequest.CancelCurrent) }
  }

  fun saveLocation(latitude: Double, longitude: Double, name: String) {
    viewModelScope.launch {
      saveLocationFlow.emit(LoadingFirst)

      if (initialLocationId == null) {
        saveLocationUseCase(latitude = latitude, longitude = longitude, name = name)
      } else {
        saveLocationUseCase(
          id = initialLocationId,
          latitude = latitude,
          longitude = longitude,
          name = name
        )
      }

      saveLocationFlow.emit(Unit.asLoadable())
    }
  }

  private suspend fun FlowCollector<Loadable<LocationPreparedToSave>>.emitSpecifiedLocation(
    latitude: Double,
    longitude: Double
  ) {
    emit(LoadingFirst)
    emit(
      LocationPreparedToSave(latitude = latitude, longitude = longitude, isUser = false)
        .asLoadable()
    )
  }

  private suspend fun FlowCollector<Loadable<LocationPreparedToSave>>.emitUserLocation() {
    emit(LoadingFirst)
    val userLatLng = getCurrentUserLatLngUseCase()
    if (userLatLng == null) {
      emit(FailedFirst(UserLatLngNotFound))
      delay(3_500L)
      emit(Empty)
    } else {
      emit(
        LocationPreparedToSave(
            latitude = userLatLng.latitude,
            longitude = userLatLng.longitude,
            isUser = true
          )
          .asLoadable()
      )
    }
  }
}
