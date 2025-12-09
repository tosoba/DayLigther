package com.trm.daylighter.feature.day

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trm.daylighter.core.common.navigation.DayNightCycleRouteParams
import com.trm.daylighter.core.common.navigation.GoldenBlueHourRouteParams
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
        when {
          isStartedFromDeeplink(
            locationIdParam = DayNightCycleRouteParams.LOCATION_ID,
            defaultParam = DayNightCycleRouteParams.DEFAULT,
          ) -> {
            emit(getNonDefaultLocationOffset(DayNightCycleRouteParams.LOCATION_ID))
          }
          isStartedFromDeeplink(
            locationIdParam = GoldenBlueHourRouteParams.LOCATION_ID,
            defaultParam = GoldenBlueHourRouteParams.DEFAULT,
          ) -> {
            emit(getNonDefaultLocationOffset(GoldenBlueHourRouteParams.LOCATION_ID))
          }
          else -> {
            emit(0)
          }
        }
      }
      .shareIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(5000L), replay = 1)

  private fun isStartedFromDeeplink(locationIdParam: String, defaultParam: String): Boolean =
    savedStateHandle.contains(locationIdParam) &&
      !savedStateHandle.get<String>(defaultParam).toBoolean()

  private suspend fun getNonDefaultLocationOffset(locationIdParam: String): Int =
    getNonDefaultLocationOffsetByIdUseCase(
      id = requireNotNull(savedStateHandle.get<String>(locationIdParam)).toLong()
    ) ?: 0

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
                  yesterday = change.yesterday,
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
                      yesterday = change.yesterday,
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
          fun now(): LocalTime = LocalTime.now(locations.data[index].zoneId)

          emit(now())
          delay(System.currentTimeMillis() % 1_000L)

          while (currentCoroutineContext().isActive) {
            now().takeIf { it.second == 0 }?.let { emit(it) }
            delay(1_000L)
          }
        }
        is WithoutData -> {
          emit(LocalTime.now())
        }
      }
    }
}
