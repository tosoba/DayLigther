package com.trm.daylighter.feature.day

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.trm.daylighter.core.domain.model.Loadable
import com.trm.daylighter.core.domain.model.Location
import com.trm.daylighter.core.domain.usecase.CalculateSunPositionTimestampUseCase
import com.trm.daylighter.core.domain.usecase.CalculateSunriseSunsetChangeUseCase
import com.trm.daylighter.core.domain.usecase.CalculateSunriseSunsetUseCase
import com.trm.daylighter.core.domain.usecase.GetAllLocationsFlowUseCase
import com.trm.daylighter.core.domain.usecase.GetNonDefaultLocationOffsetByIdUseCase
import io.mockk.every
import io.mockk.mockk
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before

class DayViewModelTests {
  private val dispatcher = StandardTestDispatcher()

  @Before
  fun before() {
    Dispatchers.setMain(dispatcher)
  }

  @After
  fun after() {
    Dispatchers.resetMain()
  }

  @Test
  fun `GIVEN empty saved state THEN initial locationIndexFlow should emit 0`() = runTest {
    viewModel(
        getAllLocationsFlowUseCase =
          mockk<GetAllLocationsFlowUseCase>().apply {
            every { this@apply() } returns emptyFlow<Loadable<List<Location>>>()
          }
      )
      .initialLocationIndexFlow
      .test {
        runCurrent()
        assertEquals(awaitItem(), 0)
        cancelAndIgnoreRemainingEvents()
      }
  }

  private fun viewModel(
    savedStateHandle: SavedStateHandle = SavedStateHandle(),
    getAllLocationsFlowUseCase: GetAllLocationsFlowUseCase,
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

  private fun calculateSunriseSunsetChangeUseCase(): CalculateSunriseSunsetChangeUseCase =
    CalculateSunriseSunsetChangeUseCase(
      CalculateSunriseSunsetUseCase(CalculateSunPositionTimestampUseCase())
    )
}
