package com.trm.daylighter.feature.day

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trm.daylighter.core.common.navigation.DayNightCycleDeepLinkParams
import com.trm.daylighter.core.domain.model.Empty
import com.trm.daylighter.core.domain.model.Loadable
import com.trm.daylighter.core.domain.model.LoadingFirst
import com.trm.daylighter.core.domain.model.Location
import com.trm.daylighter.core.domain.model.LocationSunriseSunsetChange
import com.trm.daylighter.core.domain.model.WithData
import com.trm.daylighter.core.domain.model.WithoutData
import com.trm.daylighter.core.domain.model.asLoadable
import com.trm.daylighter.core.domain.usecase.CalculateSunriseSunsetChangeUseCase
import com.trm.daylighter.core.domain.usecase.GetAllLocationsFlowUseCase
import com.trm.daylighter.core.domain.usecase.GetNonDefaultLocationOffsetByIdUseCase
import com.trm.daylighter.core.ui.model.StableLoadable
import com.trm.daylighter.core.ui.model.asStable
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalTime
import java.time.ZonedDateTime
import javax.inject.Inject
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.isActive

@HiltViewModel
class DayViewModel
@Inject
constructor(
  private val savedStateHandle: SavedStateHandle,
  getAllLocationsFlowUseCase: GetAllLocationsFlowUseCase,
  private val calculateSunriseSunsetChangeUseCase: CalculateSunriseSunsetChangeUseCase,
  private val getNonDefaultLocationOffsetByIdUseCase: GetNonDefaultLocationOffsetByIdUseCase,
) : ViewModel() {
  val initialLocationIndexFlow: SharedFlow<Int> =
    flow {
        if (
          savedStateHandle.contains(DayNightCycleDeepLinkParams.LOCATION_ID) &&
            !savedStateHandle.get<String>(DayNightCycleDeepLinkParams.DEFAULT).toBoolean()
        ) {
          val locationId =
            requireNotNull(savedStateHandle.get<String>(DayNightCycleDeepLinkParams.LOCATION_ID)).toLong()
          emit(getNonDefaultLocationOffsetByIdUseCase(locationId) ?: 0)
        } else {
          emit(0)
        }
      }
      .shareIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(5000L), replay = 1)

  val locationsFlow: SharedFlow<Loadable<List<Location>>> =
    getAllLocationsFlowUseCase()
      .shareIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(5000L), replay = 1)

  fun sunriseSunsetChangeInLocationAt(
    index: Int
  ): Flow<StableLoadable<LocationSunriseSunsetChange>> =
    locationsFlow
      .transformLatest { locations ->
        when (locations) {
          is WithData -> {
            emit(LoadingFirst)
            if (locations.data.isEmpty()) {
              emit(Empty)
              return@transformLatest
            }

            val location = locations.data[index]
            var change = calculateSunriseSunsetChangeUseCase(location)
            emit(
              LocationSunriseSunsetChange(
                  location = location,
                  today = change.today,
                  yesterday = change.yesterday
                )
                .asLoadable()
            )

            while (currentCoroutineContext().isActive) {
              val now = ZonedDateTime.now(location.zoneId)
              if (now.dayOfMonth != change.today.date.dayOfMonth) {
                change = calculateSunriseSunsetChangeUseCase(location)
                emit(
                  LocationSunriseSunsetChange(
                      location = location,
                      today = change.today,
                      yesterday = change.yesterday
                    )
                    .asLoadable()
                )
              }
              delay(1_000L)
            }
          }
          is WithoutData -> {
            emit(Empty)
          }
        }
      }
      .map(Loadable<LocationSunriseSunsetChange>::asStable)

  fun currentTimeInLocationAt(index: Int): Flow<LocalTime> =
    locationsFlow.transformLatest { locations ->
      when (locations) {
        is WithData -> {
          while (currentCoroutineContext().isActive) {
            emit(LocalTime.now(locations.data[index].zoneId))
            delay(1_000L)
          }
        }
        is WithoutData -> {
          emit(LocalTime.now())
        }
      }
    }
}
