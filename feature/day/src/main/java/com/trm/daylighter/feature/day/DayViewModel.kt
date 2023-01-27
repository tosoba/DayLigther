package com.trm.daylighter.feature.day

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trm.daylighter.domain.model.*
import com.trm.daylighter.domain.usecase.GetAllLocationsFlowUseCase
import com.trm.daylighter.domain.usecase.GetLocationSunriseSunsetChangeUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.*

@HiltViewModel
class DayViewModel
@Inject
constructor(
  private val savedStateHandle: SavedStateHandle,
  getAllLocationsFlowUseCase: GetAllLocationsFlowUseCase,
  getLocationSunriseSunsetChangeUseCase: GetLocationSunriseSunsetChangeUseCase,
) : ViewModel() {
  val locationsFlow: StateFlow<Loadable<List<Location>>> =
    getAllLocationsFlowUseCase()
      .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = Empty
      )

  val currentLocationIndexFlow: StateFlow<Int> =
    savedStateHandle.getStateFlow(SavedState.CURRENT_LOCATION_INDEX.name, 0)

  private var currentLocationIndex: Int
    get() = currentLocationIndexFlow.value
    set(value) {
      savedStateHandle[SavedState.CURRENT_LOCATION_INDEX.name] = value
    }

  val currentLocationSunriseSunset: SharedFlow<Loadable<SunriseSunsetChange>> =
    currentLocationIndexFlow
      .map { it to locationsFlow.value }
      .filterIsInstance<Pair<Int, Ready<List<Location>>>>()
      .map { (index, locations) -> locations.data[index].id }
      .flatMapLatest(getLocationSunriseSunsetChangeUseCase::invoke)
      .shareIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        replay = 1,
      )

  fun previousLocation() {
    val locationsLoadable = locationsFlow.value
    if (locationsLoadable !is Ready) return

    val currentIndex = currentLocationIndexFlow.value
    currentLocationIndex =
      if (currentIndex == 0) locationsLoadable.data.lastIndex else currentIndex - 1
  }

  fun nextLocation() {
    val locationsLoadable = locationsFlow.value
    if (locationsLoadable !is Ready) return

    val currentIndex = currentLocationIndexFlow.value
    currentLocationIndex =
      if (currentIndex == locationsLoadable.data.lastIndex) 0 else currentIndex + 1
  }

  private enum class SavedState {
    CURRENT_LOCATION_INDEX
  }
}
