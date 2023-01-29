package com.trm.daylighter.feature.day

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trm.daylighter.domain.model.*
import com.trm.daylighter.domain.usecase.GetAllLocationsFlowUseCase
import com.trm.daylighter.domain.usecase.GetLocationSunriseSunsetChangeUseCase
import com.trm.daylighter.feature.day.exception.LocationIndexOutOfBoundsException
import com.trm.daylighter.feature.day.model.LocationSunriseSunsetChange
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
  private val getLocationSunriseSunsetChangeUseCase: GetLocationSunriseSunsetChangeUseCase,
) : ViewModel() {
  private val currentLocationIndexFlow: StateFlow<Int> =
    savedStateHandle.getStateFlow(SavedState.CURRENT_LOCATION_INDEX.name, 0)

  private var currentLocationIndex: Int
    get() = currentLocationIndexFlow.value
    set(value) {
      savedStateHandle[SavedState.CURRENT_LOCATION_INDEX.name] = value
    }

  val currentLocationSunriseSunsetChange: SharedFlow<Loadable<LocationSunriseSunsetChange>> =
    currentLocationIndexFlow
      .combine(getAllLocationsFlowUseCase()) { index, locations ->
        when (locations) {
          is WithoutData -> LoadingFirst
          is WithData -> {
            when {
              locations.data.isEmpty() -> Empty
              index < locations.data.size -> Ready(locations.data[index])
              else -> FailedFirst(LocationIndexOutOfBoundsException(locations.data.size))
            }
          }
        }
      }
      .distinctUntilChanged()
      .onEach {
        if (it is Empty) {
          currentLocationIndex = 0
        } else {
          it.ifIsFailedWith<LocationIndexOutOfBoundsException> { (locationsSize) ->
            currentLocationIndex = locationsSize - 1
          }
        }
      }
      .filterNot { it is Failed }
      .transformLatest { locationLoadable ->
        when (locationLoadable) {
          is Empty -> emit(Empty)
          is Loading -> emit(LoadingFirst)
          is Ready -> {
            emitAll(
              getLocationSunriseSunsetChangeUseCase(locationLoadable.data.id).map {
                sunsetChangeLoadable ->
                when (sunsetChangeLoadable) {
                  is Empty -> Empty
                  is Loading -> LoadingFirst
                  is Ready -> {
                    Ready(
                      LocationSunriseSunsetChange(
                        location = locationLoadable.data,
                        sunriseSunsetChange = sunsetChangeLoadable.data
                      )
                    )
                  }
                  is Failed -> FailedFirst(sunsetChangeLoadable.error)
                }
              }
            )
          }
          else -> throw IllegalStateException()
        }
      }
      .shareIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        replay = 1,
      )

  private val showPreviousFlow = MutableSharedFlow<Unit>()

  fun previousLocation() {
    viewModelScope.launch { showPreviousFlow.emit(Unit) }
  }

  private val showNextFlow = MutableSharedFlow<Unit>()

  fun nextLocation() {
    viewModelScope.launch { showNextFlow.emit(Unit) }
  }

  init {
    showPreviousFlow
      .combine(
        getAllLocationsFlowUseCase().filterIsInstance<Ready<List<Location>>>().map { it.data.size }
      ) { _, size ->
        size
      }
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
      .combine(
        getAllLocationsFlowUseCase().filterIsInstance<Ready<List<Location>>>().map { it.data.size }
      ) { _, size ->
        size
      }
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

  private enum class SavedState {
    CURRENT_LOCATION_INDEX
  }
}
