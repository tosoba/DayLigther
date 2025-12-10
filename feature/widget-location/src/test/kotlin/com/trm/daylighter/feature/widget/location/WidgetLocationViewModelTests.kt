package com.trm.daylighter.feature.widget.location

import android.appwidget.AppWidgetManager
import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.trm.daylighter.core.common.navigation.WidgetLocationRouteParams
import com.trm.daylighter.core.common.navigation.WidgetType
import com.trm.daylighter.core.domain.usecase.GetAllLocationsFlowUseCase
import com.trm.daylighter.core.domain.widget.WidgetManager
import com.trm.daylighter.core.testing.rule.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlin.test.assertEquals
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class WidgetLocationViewModelTests {
  @get:Rule val mainDispatcherRule = MainDispatcherRule()

  @Test
  fun `GIVEN SavedStateHandle without location id THEN mode should be ADD`() {
    assertEquals(WidgetLocationMode.ADD, viewModel(SavedStateHandle()).mode)
  }

  @Test
  fun `GIVEN SavedStateHandle with widget id THEN mode should be EDIT`() {
    assertEquals(
      WidgetLocationMode.EDIT,
      viewModel(SavedStateHandle(mapOf(AppWidgetManager.EXTRA_APPWIDGET_ID to "63"))).mode,
    )
  }

  @Test
  fun `GIVEN null selected location THEN add day night cycle widget is not called`() {
    val widgetManager = mockk<WidgetManager>()
    viewModel(widgetManager = widgetManager).onAddDayNightCycleWidget()
    coVerify(exactly = 0) { widgetManager.addDayNightCycleWidget(any()) }
  }

  @Test
  fun `GIVEN null selected location THEN add golden blue hour widget is not called`() {
    val widgetManager = mockk<WidgetManager>()
    viewModel(widgetManager = widgetManager).onAddGoldenBlueHourWidget()
    coVerify(exactly = 0) { widgetManager.addGoldenBlueHourWidget(any()) }
  }

  @Test
  fun `GIVEN non null selected location id WHEN add day night cycle widget returns false THEN failed to add widget message is emitted`() =
    runTest {
      with(
        viewModel(
          savedStateHandle =
            SavedStateHandle(
              mapOf(WidgetLocationViewModel.SavedState.SELECTED_LOCATION_ID.name to 67L)
            ),
          widgetManager =
            mockk<WidgetManager> {
              coEvery { this@mockk.addDayNightCycleWidget(any()) } returns false
            },
        )
      ) {
        widgetStatus.test {
          runCurrent()
          onAddDayNightCycleWidget()
          assertEquals(R.string.failed_to_add_widget, awaitItem())
          cancelAndIgnoreRemainingEvents()
        }
      }
    }

  @Test
  fun `GIVEN non null selected location id WHEN add golden blue hour widget returns false THEN failed to add widget message is emitted`() =
    runTest {
      with(
        viewModel(
          savedStateHandle =
            SavedStateHandle(
              mapOf(WidgetLocationViewModel.SavedState.SELECTED_LOCATION_ID.name to 67L)
            ),
          widgetManager =
            mockk<WidgetManager> {
              coEvery { this@mockk.addGoldenBlueHourWidget(any()) } returns false
            },
        )
      ) {
        widgetStatus.test {
          runCurrent()
          onAddGoldenBlueHourWidget()
          assertEquals(R.string.failed_to_add_widget, awaitItem())
          cancelAndIgnoreRemainingEvents()
        }
      }
    }

  @Test
  fun `GIVEN non null selected location id WHEN add day night cycle widget returns true THEN no message is emitted`() =
    runTest {
      with(
        viewModel(
          savedStateHandle =
            SavedStateHandle(
              mapOf(WidgetLocationViewModel.SavedState.SELECTED_LOCATION_ID.name to 64L)
            ),
          widgetManager =
            mockk<WidgetManager> {
              coEvery { this@mockk.addDayNightCycleWidget(any()) } returns true
            },
        )
      ) {
        widgetStatus.test {
          runCurrent()
          onAddDayNightCycleWidget()
          expectNoEvents()
        }
      }
    }

  @Test
  fun `GIVEN non null selected location id WHEN add golden blue hour widget returns true THEN no message is emitted`() =
    runTest {
      with(
        viewModel(
          savedStateHandle =
            SavedStateHandle(
              mapOf(WidgetLocationViewModel.SavedState.SELECTED_LOCATION_ID.name to 64L)
            ),
          widgetManager =
            mockk<WidgetManager> {
              coEvery { this@mockk.addGoldenBlueHourWidget(any()) } returns true
            },
        )
      ) {
        widgetStatus.test {
          runCurrent()
          onAddGoldenBlueHourWidget()
          expectNoEvents()
        }
      }
    }

  @Test
  fun `GIVEN null selected location id AND day night cycle widget type THEN edit day night cycle widget is not called`() {
    val widgetManager = mockk<WidgetManager>()

    viewModel(
        savedStateHandle =
          SavedStateHandle(
            mapOf(WidgetLocationRouteParams.WIDGET_TYPE to WidgetType.DAY_NIGHT_CYCLE.name)
          ),
        widgetManager = widgetManager,
      )
      .onConfirmEditWidgetLocationClick()

    coVerify(exactly = 0) { widgetManager.editDayNightCycleWidget(any(), any()) }
  }

  @Test
  fun `GIVEN null selected location id AND golden blue hour widget type THEN edit golden blue hour widget is not called`() {
    val widgetManager = mockk<WidgetManager>()

    viewModel(
        savedStateHandle =
          SavedStateHandle(
            mapOf(WidgetLocationRouteParams.WIDGET_TYPE to WidgetType.GOLDEN_BLUE_HOUR.name)
          ),
        widgetManager = widgetManager,
      )
      .onConfirmEditWidgetLocationClick()

    coVerify(exactly = 0) { widgetManager.editGoldenBlueHourWidget(any(), any()) }
  }

  @Test
  fun `GIVEN non null selected location id AND day night cycle widget type THEN edit day night cycle widget is called and widget updated message is received`() =
    runTest {
      val selectedLocationId = 59L
      val widgetManager =
        mockk<WidgetManager> {
          every { this@mockk.editDayNightCycleWidget(any(), any()) } returns Unit
        }

      with(
        viewModel(
          savedStateHandle =
            SavedStateHandle(
              mapOf(
                WidgetLocationRouteParams.WIDGET_TYPE to WidgetType.DAY_NIGHT_CYCLE.name,
                AppWidgetManager.EXTRA_APPWIDGET_ID to "72",
                WidgetLocationViewModel.SavedState.SELECTED_LOCATION_ID.name to selectedLocationId,
              )
            ),
          widgetManager = widgetManager,
        )
      ) {
        widgetStatus.test {
          runCurrent()
          onConfirmEditWidgetLocationClick()
          assertEquals(R.string.widget_location_updated, awaitItem())
          cancelAndIgnoreRemainingEvents()
        }
      }

      verify(exactly = 1) { widgetManager.editDayNightCycleWidget(any(), eq(selectedLocationId)) }
    }

  @Test
  fun `GIVEN non null selected location id AND golden blue hour widget type THEN edit golden blue hour widget is called and widget updated message is received`() =
    runTest {
      val selectedLocationId = 59L
      val widgetManager =
        mockk<WidgetManager> {
          every { this@mockk.editGoldenBlueHourWidget(any(), any()) } returns Unit
        }

      with(
        viewModel(
          savedStateHandle =
            SavedStateHandle(
              mapOf(
                WidgetLocationRouteParams.WIDGET_TYPE to WidgetType.GOLDEN_BLUE_HOUR.name,
                AppWidgetManager.EXTRA_APPWIDGET_ID to "72",
                WidgetLocationViewModel.SavedState.SELECTED_LOCATION_ID.name to selectedLocationId,
              )
            ),
          widgetManager = widgetManager,
        )
      ) {
        widgetStatus.test {
          runCurrent()
          onConfirmEditWidgetLocationClick()
          assertEquals(R.string.widget_location_updated, awaitItem())
          cancelAndIgnoreRemainingEvents()
        }
      }

      verify(exactly = 1) { widgetManager.editGoldenBlueHourWidget(any(), eq(selectedLocationId)) }
    }

  private fun viewModel(
    savedStateHandle: SavedStateHandle = SavedStateHandle(),
    getAllLocationsFlowUseCase: GetAllLocationsFlowUseCase = emptyLocationsFlowUseCase(),
    widgetManager: WidgetManager = mockk(),
  ): WidgetLocationViewModel =
    WidgetLocationViewModel(savedStateHandle, getAllLocationsFlowUseCase, widgetManager)

  private fun emptyLocationsFlowUseCase(): GetAllLocationsFlowUseCase = mockk {
    every { this@mockk.invoke() } returns emptyFlow()
  }
}
