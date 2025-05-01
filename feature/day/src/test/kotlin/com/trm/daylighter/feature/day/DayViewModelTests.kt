package com.trm.daylighter.feature.day

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.trm.daylighter.core.common.navigation.DayNightCycleDeepLinkParams
import com.trm.daylighter.core.common.navigation.GoldenBlueHourDeepLinkParams
import com.trm.daylighter.core.domain.model.Empty
import com.trm.daylighter.core.domain.model.Loadable
import com.trm.daylighter.core.domain.model.LoadingFirst
import com.trm.daylighter.core.domain.model.Location
import com.trm.daylighter.core.domain.model.LocationSunriseSunsetChange
import com.trm.daylighter.core.domain.model.Ready
import com.trm.daylighter.core.domain.model.SunriseSunset
import com.trm.daylighter.core.domain.model.SunriseSunsetChange
import com.trm.daylighter.core.domain.usecase.CalculateSunPositionTimestampUseCase
import com.trm.daylighter.core.domain.usecase.CalculateSunriseSunsetChangeUseCase
import com.trm.daylighter.core.domain.usecase.CalculateSunriseSunsetUseCase
import com.trm.daylighter.core.domain.usecase.GetAllLocationsFlowUseCase
import com.trm.daylighter.core.domain.usecase.GetNonDefaultLocationOffsetByIdUseCase
import com.trm.daylighter.core.testing.model.testLocation
import com.trm.daylighter.core.testing.rule.MainDispatcherRule
import com.trm.daylighter.core.ui.model.StableValue
import com.trm.daylighter.core.ui.model.asStable
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Rule

class DayViewModelTests {
  @get:Rule val mainDispatcherRule = MainDispatcherRule()

  @Test
  fun `GIVEN empty saved state THEN initialLocationIndexFlow should emit 0`() = runTest {
    viewModel().initialLocationIndexFlow.test {
      runCurrent()
      assertEquals(awaitItem(), 0)
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `GIVEN day night deeplink saved state for default location THEN initialLocationIndexFlow should emit 0`() =
    runTest {
      viewModel(
          SavedStateHandle(initialState = mapOf(DayNightCycleDeepLinkParams.DEFAULT to "true"))
        )
        .initialLocationIndexFlow
        .test {
          runCurrent()
          assertEquals(awaitItem(), 0)
          cancelAndIgnoreRemainingEvents()
        }
    }

  @Test
  fun `GIVEN golden blue hour deeplink saved state for default location THEN initialLocationIndexFlow should emit 0`() =
    runTest {
      viewModel(
          SavedStateHandle(initialState = mapOf(GoldenBlueHourDeepLinkParams.DEFAULT to "true"))
        )
        .initialLocationIndexFlow
        .test {
          runCurrent()
          assertEquals(awaitItem(), 0)
          cancelAndIgnoreRemainingEvents()
        }
    }

  @Test
  fun `GIVEN day night deeplink saved state for non default location THEN getNonDefaultLocationOffsetByIdUseCase is called`() =
    runTest {
      val expectedOffset = Random.nextInt()
      val getNonDefaultLocationOffsetByIdUseCase =
        mockk<GetNonDefaultLocationOffsetByIdUseCase>().apply {
          coEvery { this@apply(any()) } returns expectedOffset
        }

      viewModel(
          savedStateHandle =
            SavedStateHandle(
              initialState =
                mapOf(
                  DayNightCycleDeepLinkParams.LOCATION_ID to "5",
                  DayNightCycleDeepLinkParams.DEFAULT to "false",
                )
            ),
          getNonDefaultLocationOffsetByIdUseCase = getNonDefaultLocationOffsetByIdUseCase,
        )
        .initialLocationIndexFlow
        .test {
          runCurrent()
          assertEquals(expectedOffset, awaitItem())
          cancelAndIgnoreRemainingEvents()
        }

      coVerify(exactly = 1) { getNonDefaultLocationOffsetByIdUseCase(5L) }
    }

  @Test
  fun `GIVEN golden blue hour deeplink saved state for non default location THEN getNonDefaultLocationOffsetByIdUseCase is called`() =
    runTest {
      val expectedOffset = Random.nextInt()
      val getNonDefaultLocationOffsetByIdUseCase =
        mockk<GetNonDefaultLocationOffsetByIdUseCase>().apply {
          coEvery { this@apply(any()) } returns expectedOffset
        }

      viewModel(
          savedStateHandle =
            SavedStateHandle(
              initialState =
                mapOf(
                  GoldenBlueHourDeepLinkParams.LOCATION_ID to "5",
                  GoldenBlueHourDeepLinkParams.DEFAULT to "false",
                )
            ),
          getNonDefaultLocationOffsetByIdUseCase = getNonDefaultLocationOffsetByIdUseCase,
        )
        .initialLocationIndexFlow
        .test {
          runCurrent()
          assertEquals(expectedOffset, awaitItem())
          cancelAndIgnoreRemainingEvents()
        }

      coVerify(exactly = 1) { getNonDefaultLocationOffsetByIdUseCase(5L) }
    }

  @Test
  fun `GIVEN empty locations THEN sunriseSunsetChangeInLocationAt should emit Empty`() {
    runTest {
      viewModel(
          getAllLocationsFlowUseCase =
            mockk<GetAllLocationsFlowUseCase>().apply {
              every { this@apply() } returns flowOf(Empty)
            }
        )
        .sunriseSunsetChangeInLocationAt(0)
        .test {
          runCurrent()
          assertEquals(Empty.asStable<LocationSunriseSunsetChange>(), awaitItem())
          cancelAndIgnoreRemainingEvents()
        }
    }
  }

  @Test
  fun `GIVEN ready locations with empty list THEN sunriseSunsetChangeInLocationAt should emit LoadingFirst and Empty`() {
    runTest {
      viewModel(
          getAllLocationsFlowUseCase =
            mockk<GetAllLocationsFlowUseCase>().apply {
              every { this@apply() } returns flowOf(Ready(emptyList()))
            }
        )
        .sunriseSunsetChangeInLocationAt(0)
        .test {
          runCurrent()
          assertEquals(LoadingFirst.asStable<LocationSunriseSunsetChange>(), awaitItem())
          assertEquals(Empty.asStable<LocationSunriseSunsetChange>(), awaitItem())
          cancelAndIgnoreRemainingEvents()
        }
    }
  }

  @Test
  fun `GIVEN single ready location THEN sunriseSunsetChangeInLocationAt 0 should emit LoadingFirst and Ready`() {
    runTest {
      viewModel(
          getAllLocationsFlowUseCase =
            mockk<GetAllLocationsFlowUseCase>().apply {
              every { this@apply() } returns flowOf(Ready(listOf(testLocation())))
            }
        )
        .sunriseSunsetChangeInLocationAt(0)
        .test {
          runCurrent()
          assertEquals(LoadingFirst.asStable<LocationSunriseSunsetChange>(), awaitItem())
          assertIs<StableValue<Ready<LocationSunriseSunsetChange>>>(awaitItem())
          cancelAndIgnoreRemainingEvents()
        }
    }
  }

  @Test
  fun `GIVEN single ready location WHEN day of month changes THEN sunriseSunsetChangeInLocationAt 0 should emit Ready after LoadingFirst and then another Ready`() {
    runTest {
      val calculateSunriseSunsetChangeUseCase =
        mockk<CalculateSunriseSunsetChangeUseCase>().apply useCase@{
          every { this@useCase(any()) } returns
            SunriseSunsetChange(
              today =
                mockk<SunriseSunset>().apply sunriseSunset@{
                  every { this@sunriseSunset.date } answers { LocalDate.now().minusDays(1L) }
                },
              yesterday = mockk(),
            )
        }

      viewModel(
          getAllLocationsFlowUseCase =
            mockk<GetAllLocationsFlowUseCase>().apply {
              every { this@apply() } returns flowOf(Ready(listOf(testLocation())))
            },
          calculateSunriseSunsetChangeUseCase = calculateSunriseSunsetChangeUseCase,
        )
        .sunriseSunsetChangeInLocationAt(0)
        .test {
          runCurrent()
          assertEquals(LoadingFirst.asStable<LocationSunriseSunsetChange>(), awaitItem())
          assertIs<StableValue<Ready<LocationSunriseSunsetChange>>>(awaitItem())
          assertIs<StableValue<Ready<LocationSunriseSunsetChange>>>(awaitItem())
          cancelAndIgnoreRemainingEvents()
        }

      coVerify(exactly = 2) { calculateSunriseSunsetChangeUseCase(any()) }
    }
  }

  @Test
  fun `GIVEN empty locations THEN currentTimeInLocationAt should return a single LocalTime`() {
    runTest {
      viewModel(
          getAllLocationsFlowUseCase =
            mockk<GetAllLocationsFlowUseCase>().apply {
              every { this@apply() } returns flowOf(Empty)
            }
        )
        .currentTimeInLocationAt(0)
        .test {
          runCurrent()
          assertIs<LocalTime>(awaitItem())
          expectNoEvents()
          cancelAndIgnoreRemainingEvents()
        }
    }
  }

  @Test
  fun `GIVEN single ready location THEN currentTimeInLocationAt should emit only LocalTimes with 0 second`() {
    mockkStatic(LocalTime::class) {
      every { LocalTime.now(any<ZoneId>()) } returnsMany
        List(20) { LocalTime.now().withSecond(if (it % 2 == 0) 0 else Random.nextInt(1, 60)) }

      runTest {
        viewModel(
            getAllLocationsFlowUseCase =
              mockk<GetAllLocationsFlowUseCase>().apply {
                every { this@apply() } returns flowOf(Ready(listOf(testLocation())))
              }
          )
          .currentTimeInLocationAt(0)
          .test {
            runCurrent()
            repeat(10) {
              val time = awaitItem()
              assertIs<LocalTime>(time)
              assertEquals(0, time.second)
            }
            expectNoEvents()
            cancelAndIgnoreRemainingEvents()
          }
      }
    }
  }

  private fun viewModel(
    savedStateHandle: SavedStateHandle = SavedStateHandle(),
    getAllLocationsFlowUseCase: GetAllLocationsFlowUseCase = emptyLocationsFlowUseCase(),
    calculateSunriseSunsetChangeUseCase: CalculateSunriseSunsetChangeUseCase =
      calculateSunriseSunsetChangeUseCase(),
    getNonDefaultLocationOffsetByIdUseCase: GetNonDefaultLocationOffsetByIdUseCase = mockk(),
  ): DayViewModel =
    DayViewModel(
      savedStateHandle = savedStateHandle,
      getAllLocationsFlowUseCase = getAllLocationsFlowUseCase,
      calculateSunriseSunsetChangeUseCase = calculateSunriseSunsetChangeUseCase,
      getNonDefaultLocationOffsetByIdUseCase = getNonDefaultLocationOffsetByIdUseCase,
    )

  private fun emptyLocationsFlowUseCase(): GetAllLocationsFlowUseCase =
    mockk<GetAllLocationsFlowUseCase>().apply {
      every { this@apply() } returns emptyFlow<Loadable<List<Location>>>()
    }

  private fun calculateSunriseSunsetChangeUseCase(): CalculateSunriseSunsetChangeUseCase =
    CalculateSunriseSunsetChangeUseCase(
      CalculateSunriseSunsetUseCase(CalculateSunPositionTimestampUseCase())
    )
}
