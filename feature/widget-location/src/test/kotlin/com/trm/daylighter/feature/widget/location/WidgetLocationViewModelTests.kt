package com.trm.daylighter.feature.widget.location

import androidx.lifecycle.SavedStateHandle
import com.trm.daylighter.core.common.navigation.WidgetLocationDeepLinkParams
import com.trm.daylighter.core.domain.model.Loadable
import com.trm.daylighter.core.domain.model.Location
import com.trm.daylighter.core.domain.usecase.GetAllLocationsFlowUseCase
import com.trm.daylighter.core.domain.widget.WidgetManager
import com.trm.daylighter.core.testing.rule.MainDispatcherRule
import io.mockk.every
import io.mockk.mockk
import kotlin.test.assertEquals
import kotlinx.coroutines.flow.emptyFlow
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
      viewModel(SavedStateHandle(mapOf(WidgetLocationDeepLinkParams.LOCATION_ID to "63"))).mode
    )
  }

  private fun viewModel(
    savedStateHandle: SavedStateHandle = SavedStateHandle(),
    getAllLocationsFlowUseCase: GetAllLocationsFlowUseCase = emptyLocationsFlowUseCase(),
    widgetManager: WidgetManager = mockk()
  ): WidgetLocationViewModel =
    WidgetLocationViewModel(savedStateHandle, getAllLocationsFlowUseCase, widgetManager)

  private fun emptyLocationsFlowUseCase(): GetAllLocationsFlowUseCase =
    mockk<GetAllLocationsFlowUseCase>().apply {
      every { this@apply() } returns emptyFlow<Loadable<List<Location>>>()
    }
}
