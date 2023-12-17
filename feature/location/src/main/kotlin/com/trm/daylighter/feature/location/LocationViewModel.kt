package com.trm.daylighter.feature.location

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trm.daylighter.core.common.model.MapDefaults
import com.trm.daylighter.core.domain.exception.LocationDisplayNameNotFound
import com.trm.daylighter.core.domain.model.*
import com.trm.daylighter.core.domain.usecase.GetCurrentUserLatLngUseCase
import com.trm.daylighter.core.domain.usecase.GetGeocodingEmailFlowUseCase
import com.trm.daylighter.core.domain.usecase.GetLocationByIdUseCase
import com.trm.daylighter.core.domain.usecase.GetLocationDisplayNameUseCase
import com.trm.daylighter.core.domain.usecase.SaveLocationUseCase
import com.trm.daylighter.core.domain.usecase.SetGeocodingEmailUseCase
import com.trm.daylighter.feature.location.exception.UserLatLngNotFound
import com.trm.daylighter.feature.location.model.*
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber

@HiltViewModel
class LocationViewModel
@Inject
constructor(
  private val savedStateHandle: SavedStateHandle,
  private val getLocationByIdUseCase: GetLocationByIdUseCase,
  private val saveLocationUseCase: SaveLocationUseCase,
  private val getCurrentUserLatLngUseCase: GetCurrentUserLatLngUseCase,
  private val getLocationDisplayNameUseCase: GetLocationDisplayNameUseCase,
  private val setGeocodingEmailUseCase: SetGeocodingEmailUseCase,
  getGeocodingEmailFlowUseCase: GetGeocodingEmailFlowUseCase,
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
      .onEach {
        if (it !is Ready) return@onEach

        val (latitude, longitude, isUser) = it.data
        if (!isUser) return@onEach

        savedStateHandle.get<MapPosition>(MAP_POSITION)?.let { position ->
          savedStateHandle[MAP_POSITION] =
            position.copy(
              latitude = latitude,
              longitude = longitude,
              zoom = MapDefaults.INITIAL_LOCATION_ZOOM,
              uuid = UUID.randomUUID()
            )
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

  val mapPositionFlow: StateFlow<MapPosition> =
    savedStateHandle.getStateFlow(MAP_POSITION, MapPosition())

  fun onMapViewPause(mapPosition: MapPosition) {
    savedStateHandle[MAP_POSITION] = mapPosition
  }

  init {
    initialLocationId?.let {
      viewModelScope.launch {
        val location = getLocationByIdUseCase(it) ?: return@launch
        savedStateHandle[MAP_POSITION] =
          MapPosition(
            latitude = location.latitude,
            longitude = location.longitude,
            zoom = MapDefaults.INITIAL_LOCATION_ZOOM,
            label = location.name
          )
      }
    }
  }

  private val locationNameRequestFlow = MutableSharedFlow<LocationNameRequest>()
  private val locationNameFlow: SharedFlow<Loadable<String>> =
    locationNameRequestFlow
      .flatMapLatest { request ->
        when (request) {
          is LocationNameRequest.Geocode -> {
            channelFlow {
              var failed = false
              getLocationDisplayNameUseCase(lat = request.lat, lng = request.lng).collectLatest {
                failed = it is Failed
                if (it is Failed) Timber.e(it.throwable)
                send(it)
              }
              if (failed) {
                delay(3_500L)
                send(Empty)
              }
            }
          }
          is LocationNameRequest.UserInput -> {
            flowOf(request.name.asLoadable())
          }
          LocationNameRequest.Clear -> {
            flowOf(Empty)
          }
        }
      }
      .shareIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(5000L), replay = 1)

  val locationNameFailureMessageFlow: Flow<Int?> =
    locationNameFlow.map {
      when {
        it.isFailedWith<LocationDisplayNameNotFound>() -> R.string.location_name_not_found
        it is Failed -> R.string.geocoding_error
        else -> null
      }
    }
  val locationNameReadyFlow: Flow<String> =
    locationNameFlow.map { if (it is Ready) it.data else "" }
  val locationNameLoadingFlow: Flow<Boolean> = locationNameFlow.map { it is Loading }

  val screenMode: LocationScreenMode =
    if (initialLocationId == null) LocationScreenMode.ADD else LocationScreenMode.EDIT

  val isGeocodeEmailPreferenceSetFlow: SharedFlow<Boolean> =
    getGeocodingEmailFlowUseCase()
      .map { it != null }
      .shareIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(5000L), replay = 1)

  val geocodingEmailFlow: SharedFlow<String?> =
    getGeocodingEmailFlowUseCase()
      .shareIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(5000L), replay = 1)

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

  fun getLocationDisplayName(latitude: Double, longitude: Double) {
    viewModelScope.launch {
      locationNameRequestFlow.emit(LocationNameRequest.Geocode(lat = latitude, lng = longitude))
    }
  }

  fun inputLocationName(name: String) {
    viewModelScope.launch {
      locationNameRequestFlow.emit(LocationNameRequest.UserInput(name = name))
    }
  }

  fun clearLocationName() {
    viewModelScope.launch { locationNameRequestFlow.emit(LocationNameRequest.Clear) }
  }

  fun setGeocodingEmail(email: String) {
    viewModelScope.launch { setGeocodingEmailUseCase(email) }
  }

  private suspend fun FlowCollector<Loadable<LocationPreparedToSave>>.emitSpecifiedLocation(
    latitude: Double,
    longitude: Double
  ) {
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

  companion object {
    private const val MAP_POSITION = "MAP_POSITION"
  }
}
