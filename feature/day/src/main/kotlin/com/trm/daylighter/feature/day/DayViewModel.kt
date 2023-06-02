package com.trm.daylighter.feature.day

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trm.daylighter.core.common.navigation.DayDeepLinkParams
import com.trm.daylighter.core.common.util.ext.takeIfInstance
import com.trm.daylighter.core.common.util.ext.withLatestFrom
import com.trm.daylighter.core.domain.model.*
import com.trm.daylighter.core.domain.usecase.GetLocationSunriseSunsetChangeAtIndexFlowUseCase
import com.trm.daylighter.core.domain.usecase.GetLocationsCountFlowUseCase
import com.trm.daylighter.core.domain.usecase.GetNonDefaultLocationOffsetByIdUseCase
import com.trm.daylighter.core.domain.usecase.ReceiveLocationSavedEventUseCase
import com.trm.daylighter.core.ui.model.StableLoadable
import com.trm.daylighter.core.ui.model.asStable
import com.trm.daylighter.feature.day.exception.LocationIndexOutOfBoundsException
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalTime
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
  private val getLocationSunriseSunsetChangeAtIndexFlowUseCase:
    GetLocationSunriseSunsetChangeAtIndexFlowUseCase,
  private val getNonDefaultLocationOffsetByIdUseCase: GetNonDefaultLocationOffsetByIdUseCase,
  receiveLocationSavedEventUseCase: ReceiveLocationSavedEventUseCase,
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

  init {
    val argsCount =
      savedStateHandle.keys().count {
        it == DayDeepLinkParams.LOCATION_ID || it == DayDeepLinkParams.DEFAULT
      }
    if (
      argsCount == 2 && !savedStateHandle.get<String>(DayDeepLinkParams.DEFAULT).toBoolean()
    ) {
      viewModelScope.launch {
        getNonDefaultLocationOffsetByIdUseCase(
            id = savedStateHandle.get<Long>(DayDeepLinkParams.LOCATION_ID)!!
          )
          ?.let(::currentLocationIndex::set)
      }
    }
  }

  private val reloadLocationFlow = MutableSharedFlow<Unit>()

  val currentLocationSunriseSunsetChangeFlow:
    SharedFlow<StableLoadable<LocationSunriseSunsetChange>> =
    buildCurrentLocationSunriseSunsetChangeFlow()

  val currentTimeAtCurrentLocation: SharedFlow<LocalTime> =
    currentLocationSunriseSunsetChangeFlow
      .map { it.value }
      .filterIsInstance<Ready<LocationSunriseSunsetChange>>()
      .map { it.data.location }
      .transformLatest { location ->
        while (currentCoroutineContext().isActive) {
          emit(LocalTime.now(location.zoneId))
          delay(60_000L)
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

    receiveLocationSavedEventUseCase()
      .withLatestFrom(
        currentLocationSunriseSunsetChangeFlow
          .map { it.value }
          .filterIsInstance<Ready<LocationSunriseSunsetChange>>()
          .map { it.data }
      ) { id, (location, _, _) ->
        if (id != location.id) null else Unit
      }
      .filterNotNull()
      .onEach { reloadLocation() }
      .launchIn(viewModelScope)
  }

  fun changeLocation(index: Int) {
    viewModelScope.launch { changeLocationIndexFlow.emit(index) }
  }

  fun reloadLocation() {
    viewModelScope.launch { reloadLocationFlow.emit(Unit) }
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

    val reloadedFlow: Flow<Loadable<LocationSunriseSunsetChange>> =
      reloadLocationFlow
        .map { currentLocationIndex }
        .withLatestFrom(locationCountFlow, ::Pair)
        .transformToLocationSunriseSunsetChange()

    val currentLocationSunriseSunsetChangeFlow: SharedFlow<Loadable<LocationSunriseSunsetChange>> =
      merge(atCurrentIndexFlow, reloadedFlow)
        .shareIn(scope = viewModelScope, started = SharingStarted.Eagerly)

    val loadNextLocationSunriseSunsetChangeAfterMidnightFlow =
      currentLocationSunriseSunsetChangeFlow
        .filterIsInstance<Ready<LocationSunriseSunsetChange>>()
        .map { it.data }
        .transformLatest { (location, today, _) ->
          while (currentCoroutineContext().isActive) {
            val now = ZonedDateTime.now(location.zoneId)
            if (now.dayOfMonth != today.date.dayOfMonth) {
              emitAll(
                getLocationSunriseSunsetChangeAtIndexFlowUseCase(index = currentLocationIndex)
              )
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
          getLocationSunriseSunsetChangeAtIndexFlowUseCase(index).map {
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
