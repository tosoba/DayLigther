package com.trm.daylighter.feature.day

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trm.daylighter.core.common.util.takeIfInstance
import com.trm.daylighter.core.common.util.withLatestFrom
import com.trm.daylighter.domain.model.*
import com.trm.daylighter.domain.model.LocationSunriseSunsetChange
import com.trm.daylighter.domain.usecase.GetAllLocationsFlowUseCase
import com.trm.daylighter.domain.usecase.GetLocationSunriseSunsetChangeUseCase
import com.trm.daylighter.domain.usecase.GetLocationsCountFlowUseCase
import com.trm.daylighter.feature.day.exception.LocationIndexOutOfBoundsException
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@HiltViewModel
class DayViewModel
@Inject
constructor(
  private val savedStateHandle: SavedStateHandle,
  getAllLocationsFlowUseCase: GetAllLocationsFlowUseCase,
  private val getLocationsCountFlowUseCase: GetLocationsCountFlowUseCase,
  private val getLocationSunriseSunsetChangeUseCase: GetLocationSunriseSunsetChangeUseCase,
) : ViewModel() {
  val locationNavigationEnabledFlow: SharedFlow<Boolean> =
    getLocationsCountFlowUseCase()
      .map { it > 1 }
      .shareIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(5000L), replay = 1)

  private val currentLocationIndexFlow: StateFlow<Int> =
    savedStateHandle.getStateFlow(SavedState.CURRENT_LOCATION_INDEX.name, 0)

  private var currentLocationIndex: Int
    get() = currentLocationIndexFlow.value
    set(value) {
      savedStateHandle[SavedState.CURRENT_LOCATION_INDEX.name] = value
    }

  private val retryFlow = MutableSharedFlow<Unit>()

  private val currentLocationFlow: Flow<Loadable<Location>> =
    currentLocationIndexFlow
      .combine(getAllLocationsFlowUseCase()) { index, locations -> locations[index] }
      .distinctUntilChanged()
      .onEach { location ->
        if (location is Empty) {
          currentLocationIndex = 0
        } else if (location is Failed) {
          location.throwable?.takeIfInstance<LocationIndexOutOfBoundsException>()?.let {
            (locationsSize) ->
            currentLocationIndex = locationsSize - 1
          }
        }
      }
      .filterNot { it is Failed }

  private val retryLocationFlow: Flow<Loadable<Location>> =
    retryFlow
      .map { currentLocationIndex }
      .withLatestFrom(getAllLocationsFlowUseCase()) { index, locations -> locations[index] }
      .filterNot { it is Failed }

  val currentLocationSunriseSunsetChange: SharedFlow<Loadable<LocationSunriseSunsetChange>> =
    merge(currentLocationFlow, retryLocationFlow)
      .transformLatest { location ->
        when (location) {
          is Empty -> emit(Empty)
          is Loading -> emit(LoadingFirst)
          is Ready -> emitAll(getLocationSunriseSunsetChangeUseCase(locationId = location.data.id))
          else -> throw IllegalStateException()
        }
      }
      .debounce(250L)
      .shareIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        replay = 1,
      )

  private val showPreviousFlow = MutableSharedFlow<Unit>()
  private val showNextFlow = MutableSharedFlow<Unit>()

  init {
    showPreviousFlow
      .withLatestLocationsCount()
      .onEach { size ->
        currentLocationIndex =
          when {
            size == 0 -> 0
            currentLocationIndex == 0 -> size - 1
            else -> currentLocationIndex - 1
          }
      }
      .launchIn(viewModelScope)

    showNextFlow
      .withLatestLocationsCount()
      .onEach { size ->
        currentLocationIndex =
          when {
            size == 0 -> 0
            currentLocationIndex == size - 1 -> 0
            else -> currentLocationIndex + 1
          }
      }
      .launchIn(viewModelScope)
  }

  fun previousLocation() {
    viewModelScope.launch { showPreviousFlow.emit(Unit) }
  }

  fun nextLocation() {
    viewModelScope.launch { showNextFlow.emit(Unit) }
  }

  fun retry() {
    viewModelScope.launch { retryFlow.emit(Unit) }
  }

  private fun Flow<*>.withLatestLocationsCount(): Flow<Int> =
    withLatestFrom(getLocationsCountFlowUseCase()) { _, size -> size }

  private enum class SavedState {
    CURRENT_LOCATION_INDEX
  }
}

private operator fun Loadable<List<Location>>.get(index: Int): Loadable<Location> =
  when (this) {
    is WithoutData -> LoadingFirst
    is WithData -> {
      when {
        data.isEmpty() -> Empty
        index < data.size -> Ready(data[index])
        else -> FailedFirst(LocationIndexOutOfBoundsException(data.size))
      }
    }
  }
