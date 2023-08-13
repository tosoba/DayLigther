package com.trm.daylighter.feature.day

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.trm.daylighter.core.common.navigation.DayNightCycleDeepLinkParams
import com.trm.daylighter.core.common.navigation.GoldenBlueHourDeepLinkParams
import com.trm.daylighter.core.domain.model.Loadable
import com.trm.daylighter.core.domain.model.Location
import com.trm.daylighter.core.domain.usecase.CalculateSunPositionTimestampUseCase
import com.trm.daylighter.core.domain.usecase.CalculateSunriseSunsetChangeUseCase
import com.trm.daylighter.core.domain.usecase.CalculateSunriseSunsetUseCase
import com.trm.daylighter.core.domain.usecase.GetAllLocationsFlowUseCase
import com.trm.daylighter.core.domain.usecase.GetNonDefaultLocationOffsetByIdUseCase
import com.trm.daylighter.core.testing.util.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.flow.emptyFlow
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
                  DayNightCycleDeepLinkParams.DEFAULT to "false"
                )
            ),
          getNonDefaultLocationOffsetByIdUseCase = getNonDefaultLocationOffsetByIdUseCase
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
                  GoldenBlueHourDeepLinkParams.DEFAULT to "false"
                )
            ),
          getNonDefaultLocationOffsetByIdUseCase = getNonDefaultLocationOffsetByIdUseCase
        )
        .initialLocationIndexFlow
        .test {
          runCurrent()
          assertEquals(expectedOffset, awaitItem())
          cancelAndIgnoreRemainingEvents()
        }

      coVerify(exactly = 1) { getNonDefaultLocationOffsetByIdUseCase(5L) }
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
      getNonDefaultLocationOffsetByIdUseCase = getNonDefaultLocationOffsetByIdUseCase
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
