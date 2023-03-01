package com.trm.daylighter.feature.day

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trm.daylighter.core.common.util.takeIfInstance
import com.trm.daylighter.core.common.util.withLatestFrom
import com.trm.daylighter.core.domain.model.*
import com.trm.daylighter.core.domain.usecase.EnqueueSyncUseCase
import com.trm.daylighter.core.domain.usecase.GetLocationSunriseSunsetChangeAtIndexUseCase
import com.trm.daylighter.core.domain.usecase.GetLocationsCountFlowUseCase
import com.trm.daylighter.core.ui.model.StableLoadable
import com.trm.daylighter.core.ui.model.asStable
import com.trm.daylighter.feature.day.exception.LocationIndexOutOfBoundsException
import com.trm.daylighter.feature.day.ext.getUpcomingTimestampsSorted
import com.trm.daylighter.feature.day.ext.now
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.ZonedDateTime
import java.util.*
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
  private val getLocationSunriseSunsetChangeAtIndexUseCase:
    GetLocationSunriseSunsetChangeAtIndexUseCase,
  private val enqueueSyncUseCase: EnqueueSyncUseCase,
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
    SharedFlow<StableLoadable<LocationSunriseSunsetChange>> =
    buildCurrentLocationSunriseSunsetChangeFlow()

  val nowAtCurrentLocation: SharedFlow<ZonedDateTime> =
    currentLocationSunriseSunsetChangeFlow
      .map { it.value }
      .filterIsInstance<Ready<LocationSunriseSunsetChange>>()
      .map { it.data.today }
      .transformLatest { today ->
        val initialNow = today.now()
        emit(initialNow)

        val remainingTimestamps = LinkedList(today.getUpcomingTimestampsSorted(initialNow))
        while (currentCoroutineContext().isActive && remainingTimestamps.isNotEmpty()) {
          val now = today.now()
          if (remainingTimestamps.first().isBefore(now)) {
            remainingTimestamps.removeFirst()
            emit(now)
          }
          delay(1000L)
        }
      }
      .shareIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(5000L), replay = 1)

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
    SharedFlow<StableLoadable<LocationSunriseSunsetChange>> {
    val atCurrentIndexFlow: Flow<Loadable<LocationSunriseSunsetChange>> =
      currentLocationIndexFlow
        .combine(locationCountFlow, ::Pair)
        .distinctUntilChanged()
        .transformToLocationSunriseSunsetChange()
        .onEach { loadable ->
          if (loadable is Empty) {
            currentLocationIndex = 0
          } else if (loadable is Failed) {
            loadable.throwable?.takeIfInstance<LocationIndexOutOfBoundsException>()?.let {
              (locationsSize) ->
              currentLocationIndex = if (locationsSize > 0) locationsSize - 1 else 0
            }
          }
        }

    val retriedFlow: Flow<Loadable<LocationSunriseSunsetChange>> =
      retryFlow
        .map { currentLocationIndex }
        .withLatestFrom(locationCountFlow, ::Pair)
        .transformToLocationSunriseSunsetChange()

    val currentLocationSunriseSunsetChangeFlow: SharedFlow<Loadable<LocationSunriseSunsetChange>> =
      merge(atCurrentIndexFlow, retriedFlow)
        .shareIn(scope = viewModelScope, started = SharingStarted.Eagerly)

    val loadNextLocationSunriseSunsetChangeAfterMidnightFlow =
      currentLocationSunriseSunsetChangeFlow
        .filterIsInstance<Ready<LocationSunriseSunsetChange>>()
        .map { it.data }
        .transformLatest { (location, today, _) ->
          while (currentCoroutineContext().isActive) {
            val now = ZonedDateTime.now(location.zoneId)
            if (now.dayOfMonth != today.date.dayOfMonth) {
              emitAll(getLocationSunriseSunsetChangeAtIndexUseCase(index = currentLocationIndex))
            }
            delay(1000L)
          }
        }

    return merge(
        currentLocationSunriseSunsetChangeFlow,
        loadNextLocationSunriseSunsetChangeAfterMidnightFlow
      )
      .map(Loadable<LocationSunriseSunsetChange>::asStable)
      .debounce(250L)
      .onEach {
        if (it.value is Ready) {
          viewModelScope.launch {
            delay(1_000L)
            enqueueSyncUseCase()
          }
        }
      }
      .shareIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        replay = 1,
      )
  }

  private fun Flow<Pair<Int, Int>>.transformToLocationSunriseSunsetChange():
    Flow<Loadable<LocationSunriseSunsetChange>> = transformLatest { (index, size) ->
    when {
      size == 0 -> emit(Empty)
      index >= size -> emit(FailedFirst(LocationIndexOutOfBoundsException(size)))
      else -> {
        emit(LoadingFirst)
        emitAll(
          getLocationSunriseSunsetChangeAtIndexUseCase(index).map {
            if (it is Empty) FailedFirst(LocationIndexOutOfBoundsException(size)) else it
          }
        )
      }
    }
  }

  private enum class SavedState {
    CURRENT_LOCATION_INDEX
  }
}
