package com.trm.daylighter.feature.day

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trm.daylighter.core.common.util.takeIfInstance
import com.trm.daylighter.core.common.util.withLatestFrom
import com.trm.daylighter.core.domain.model.*
import com.trm.daylighter.core.domain.model.LocationSunriseSunsetChange
import com.trm.daylighter.core.domain.usecase.GetLocationAtIndexUseCase
import com.trm.daylighter.core.domain.usecase.GetLocationSunriseSunsetChangeUseCase
import com.trm.daylighter.core.domain.usecase.GetLocationsCountFlowUseCase
import com.trm.daylighter.core.ui.model.StableValue
import com.trm.daylighter.core.ui.model.asStable
import com.trm.daylighter.feature.day.exception.LocationIndexOutOfBoundsException
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.ZonedDateTime
import javax.inject.Inject
import kotlin.math.max
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

@HiltViewModel
class DayViewModel
@Inject
constructor(
  private val savedStateHandle: SavedStateHandle,
  getLocationsCountFlowUseCase: GetLocationsCountFlowUseCase,
  private val getLocationAtIndexUseCase: GetLocationAtIndexUseCase,
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

  val currentLocationSunriseSunsetChangeFlow:
    SharedFlow<Loadable<StableValue<LocationSunriseSunsetChange>>> =
    buildCurrentLocationSunriseSunsetChangeFlow()

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

  private fun buildCurrentLocationSunriseSunsetChangeFlow():
    SharedFlow<Loadable<StableValue<LocationSunriseSunsetChange>>> {
    val currentLocationFlow: Flow<Loadable<Location>> =
      currentLocationIndexFlow
        .combine(locationCountFlow, ::Pair)
        .distinctUntilChanged()
        .transformToLocation()
        .onEach { location ->
          if (location is Empty) {
            currentLocationIndex = 0
          } else if (location is Failed) {
            location.throwable?.takeIfInstance<LocationIndexOutOfBoundsException>()?.let {
              (locationsSize) ->
              currentLocationIndex = if (locationsSize > 0) locationsSize - 1 else 0
            }
          }
        }
        .filterNot { it is Failed }

    val retryLocationFlow: Flow<Loadable<Location>> =
      retryFlow
        .map { currentLocationIndex }
        .withLatestFrom(locationCountFlow, ::Pair)
        .transformToLocation()
        .filterNot { it is Failed }

    val currentLocationSunriseSunsetChangeFlow =
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
        .shareIn(scope = viewModelScope, started = SharingStarted.Eagerly)

    val loadNextLocationSunriseSunsetChangeAfterMidnightFlow =
      currentLocationSunriseSunsetChangeFlow
        .filterIsInstance<Ready<StableValue<LocationSunriseSunsetChange>>>()
        .map { it.data.value }
        .transformLatest { (location, today, _) ->
          while (currentCoroutineContext().isActive) {
            val now = ZonedDateTime.now(location.zoneId)
            if (now.dayOfMonth != today.date.dayOfMonth) {
              emit(location.id)
            }
            delay(1000L)
          }
        }
        .transformLatest { locationId ->
          emitAll(
            getLocationSunriseSunsetChangeUseCase(locationId = locationId).map {
              it.map(LocationSunriseSunsetChange::asStable)
            }
          )
        }

    return merge(
        currentLocationSunriseSunsetChangeFlow,
        loadNextLocationSunriseSunsetChangeAfterMidnightFlow
      )
      .debounce(250L)
      .shareIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        replay = 1,
      )
  }

  private fun Flow<Pair<Int, Int>>.transformToLocation(): Flow<Loadable<Location>> =
    transformLatest { (index, size) ->
      when {
        size == 0 -> emit(Empty)
        index >= size -> emit(FailedFirst(LocationIndexOutOfBoundsException(size)))
        else -> {
          emit(LoadingFirst)
          emit(
            getLocationAtIndexUseCase(index)?.let(::Ready)
              ?: FailedFirst(LocationIndexOutOfBoundsException(size))
          )
        }
      }
    }

  private enum class SavedState {
    CURRENT_LOCATION_INDEX
  }
}
