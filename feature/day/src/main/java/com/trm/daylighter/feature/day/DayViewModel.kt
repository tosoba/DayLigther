package com.trm.daylighter.feature.day

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trm.daylighter.core.common.util.takeIfInstance
import com.trm.daylighter.core.common.util.withLatestFrom
import com.trm.daylighter.core.domain.model.*
import com.trm.daylighter.core.domain.model.LocationSunriseSunsetChange
import com.trm.daylighter.core.domain.usecase.GetAllLocationsFlowUseCase
import com.trm.daylighter.core.domain.usecase.GetLocationSunriseSunsetChangeUseCase
import com.trm.daylighter.core.domain.usecase.GetLocationsCountFlowUseCase
import com.trm.daylighter.core.ui.model.StableValue
import com.trm.daylighter.core.ui.model.asStable
import com.trm.daylighter.feature.day.exception.LocationIndexOutOfBoundsException
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlin.math.max
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@HiltViewModel
class DayViewModel
@Inject
constructor(
  private val savedStateHandle: SavedStateHandle,
  getAllLocationsFlowUseCase: GetAllLocationsFlowUseCase,
  getLocationsCountFlowUseCase: GetLocationsCountFlowUseCase,
  private val getLocationSunriseSunsetChangeUseCase: GetLocationSunriseSunsetChangeUseCase,
) : ViewModel() {
  val locationCountFlow: SharedFlow<Int> =
    getLocationsCountFlowUseCase()
      .shareIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(5000L), replay = 1)

  private val currentLocationIndexFlow: StateFlow<Int> =
    savedStateHandle.getStateFlow(SavedState.CURRENT_LOCATION_INDEX.name, 0)

  var currentLocationIndex: Int
    get() = currentLocationIndexFlow.value
    private set(value) {
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

  val currentLocationSunriseSunsetChange:
    SharedFlow<Loadable<StableValue<LocationSunriseSunsetChange>>> =
    merge(currentLocationFlow, retryLocationFlow)
      .transformLatest { location ->
        when (location) {
          is Empty -> emit(Empty)
          is Loading -> emit(LoadingFirst)
          is Ready -> {
            emitAll(
              getLocationSunriseSunsetChangeUseCase(locationId = location.data.id).map {
                it.map(LocationSunriseSunsetChange::asStable)
              }
            )
          }
          else -> throw IllegalStateException()
        }
      }
      .debounce(250L)
      .shareIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        replay = 1,
      )

  private val changeLocationIndexFlow = MutableSharedFlow<Int>()

  init {
    changeLocationIndexFlow
      .withLatestFrom(getLocationsCountFlowUseCase()) { index, count -> index to count }
      .onEach { (index, count) ->
        currentLocationIndex =
          when {
            index < 0 -> max(0, count - 1)
            index < count -> index
            else -> 0
          }
      }
      .launchIn(viewModelScope)
  }

  fun changeLocation(index: Int) {
    viewModelScope.launch { changeLocationIndexFlow.emit(index) }
  }

  fun retry() {
    viewModelScope.launch { retryFlow.emit(Unit) }
  }

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
