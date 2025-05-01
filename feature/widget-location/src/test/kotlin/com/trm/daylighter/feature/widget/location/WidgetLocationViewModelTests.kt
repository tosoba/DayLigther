package com.trm.daylighter.feature.widget.location

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.trm.daylighter.core.common.navigation.WidgetLocationDeepLinkParams
import com.trm.daylighter.core.common.navigation.WidgetTypeParam
import com.trm.daylighter.core.domain.model.Loadable
import com.trm.daylighter.core.domain.model.Location
import com.trm.daylighter.core.domain.usecase.GetAllLocationsFlowUseCase
import com.trm.daylighter.core.domain.widget.WidgetManager
import com.trm.daylighter.core.testing.rule.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
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
  fun `GIVEN SavedStateHandle with location id THEN mode should be EDIT`() {
    assertEquals(
      WidgetLocationMode.EDIT,
      viewModel(SavedStateHandle(mapOf(WidgetLocationDeepLinkParams.LOCATION_ID to "63"))).mode,
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
            mockk<WidgetManager>().apply {
              coEvery { this@apply.addDayNightCycleWidget(any()) } returns false
            },
        )
      ) {
        toastMessageResId.test {
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
            mockk<WidgetManager>().apply {
              coEvery { this@apply.addGoldenBlueHourWidget(any()) } returns false
            },
        )
      ) {
        toastMessageResId.test {
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
            mockk<WidgetManager>().apply {
              coEvery { this@apply.addDayNightCycleWidget(any()) } returns true
            },
        )
      ) {
        toastMessageResId.test {
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
            mockk<WidgetManager>().apply {
              coEvery { this@apply.addGoldenBlueHourWidget(any()) } returns true
            },
        )
      ) {
        toastMessageResId.test {
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
            mapOf(WidgetLocationDeepLinkParams.WIDGET_TYPE to WidgetTypeParam.DAY_NIGHT_CYCLE.name)
          ),
        widgetManager = widgetManager,
      )
      .onEditWidgetLocationClick()

    coVerify(exactly = 0) { widgetManager.editDayNightCycleWidget(any(), any()) }
  }

  @Test
  fun `GIVEN null selected location id AND golden blue hour widget type THEN edit golden blue hour widget is not called`() {
    val widgetManager = mockk<WidgetManager>()

    viewModel(
        savedStateHandle =
          SavedStateHandle(
            mapOf(WidgetLocationDeepLinkParams.WIDGET_TYPE to WidgetTypeParam.GOLDEN_BLUE_HOUR.name)
          ),
        widgetManager = widgetManager,
      )
      .onEditWidgetLocationClick()

    coVerify(exactly = 0) { widgetManager.editGoldenBlueHourWidget(any(), any()) }
  }

  @Test
  fun `GIVEN non null selected location id AND day night cycle widget type THEN edit day night cycle widget is called and widget updated message is received`() =
    runTest {
      val selectedLocationId = 59L
      val widgetManager =
        mockk<WidgetManager>().apply {
          coEvery { this@apply.editDayNightCycleWidget(any(), any()) } returns Unit
        }

      with(
        viewModel(
          savedStateHandle =
            SavedStateHandle(
              mapOf(
                WidgetLocationDeepLinkParams.WIDGET_TYPE to WidgetTypeParam.DAY_NIGHT_CYCLE.name,
                WidgetLocationDeepLinkParams.GLANCE_ID to "72",
                WidgetLocationViewModel.SavedState.SELECTED_LOCATION_ID.name to selectedLocationId,
              )
            ),
          widgetManager = widgetManager,
        )
      ) {
        toastMessageResId.test {
          runCurrent()
          onEditWidgetLocationClick()
          assertEquals(R.string.widget_location_updated, awaitItem())
          cancelAndIgnoreRemainingEvents()
        }
      }

      coVerify(exactly = 1) { widgetManager.editDayNightCycleWidget(any(), eq(selectedLocationId)) }
    }

  @Test
  fun `GIVEN non null selected location id AND golden blue hour widget type THEN edit golden blue hour widget is called and widget updated message is received`() =
    runTest {
      val selectedLocationId = 59L
      val widgetManager =
        mockk<WidgetManager>().apply {
          coEvery { this@apply.editGoldenBlueHourWidget(any(), any()) } returns Unit
        }

      with(
        viewModel(
          savedStateHandle =
            SavedStateHandle(
              mapOf(
                WidgetLocationDeepLinkParams.WIDGET_TYPE to WidgetTypeParam.GOLDEN_BLUE_HOUR.name,
                WidgetLocationDeepLinkParams.GLANCE_ID to "72",
                WidgetLocationViewModel.SavedState.SELECTED_LOCATION_ID.name to selectedLocationId,
              )
            ),
          widgetManager = widgetManager,
        )
      ) {
        toastMessageResId.test {
          runCurrent()
          onEditWidgetLocationClick()
          assertEquals(R.string.widget_location_updated, awaitItem())
          cancelAndIgnoreRemainingEvents()
        }
      }

      coVerify(exactly = 1) {
        widgetManager.editGoldenBlueHourWidget(any(), eq(selectedLocationId))
      }
    }

  private fun viewModel(
    savedStateHandle: SavedStateHandle = SavedStateHandle(),
    getAllLocationsFlowUseCase: GetAllLocationsFlowUseCase = emptyLocationsFlowUseCase(),
    widgetManager: WidgetManager = mockk(),
  ): WidgetLocationViewModel =
    WidgetLocationViewModel(savedStateHandle, getAllLocationsFlowUseCase, widgetManager)

  private fun emptyLocationsFlowUseCase(): GetAllLocationsFlowUseCase =
    mockk<GetAllLocationsFlowUseCase>().apply {
      every { this@apply() } returns emptyFlow<Loadable<List<Location>>>()
    }
}
