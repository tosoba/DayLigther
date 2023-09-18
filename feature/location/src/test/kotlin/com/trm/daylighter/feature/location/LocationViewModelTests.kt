package com.trm.daylighter.feature.location

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.trm.daylighter.core.domain.usecase.GetCurrentUserLatLngUseCase
import com.trm.daylighter.core.domain.usecase.GetLocationById
import com.trm.daylighter.core.domain.usecase.GetLocationDisplayName
import com.trm.daylighter.core.domain.usecase.IsGeocodingEmailPreferenceSetFlowUseCase
import com.trm.daylighter.core.domain.usecase.SaveLocationUseCase
import com.trm.daylighter.feature.location.model.MapPosition
import io.mockk.every
import io.mockk.mockk
import kotlin.test.assertEquals
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Test

class LocationViewModelTests {
  @Test
  fun `GIVEN no initial location id THEN map position flow should only emit default map position`() =
    runTest {
      viewModel().mapPositionFlow.test {
        runCurrent()
        assertMapPositionEquals(MapPosition(), awaitItem())
        cancelAndIgnoreRemainingEvents()
      }
    }

  private fun viewModel(
    savedStateHandle: SavedStateHandle = SavedStateHandle(),
    getLocationById: GetLocationById = mockk(),
    saveLocationUseCase: SaveLocationUseCase = mockk(),
    getCurrentUserLatLngUseCase: GetCurrentUserLatLngUseCase = mockk(),
    getLocationDisplayName: GetLocationDisplayName = mockk(),
    isGeocodingEmailPreferenceSetFlowUseCase: IsGeocodingEmailPreferenceSetFlowUseCase =
      mockk<IsGeocodingEmailPreferenceSetFlowUseCase>().apply {
        every { this@apply() } returns flowOf(false)
      }
  ): LocationViewModel =
    LocationViewModel(
      savedStateHandle,
      getLocationById,
      saveLocationUseCase,
      getCurrentUserLatLngUseCase,
      getLocationDisplayName,
      isGeocodingEmailPreferenceSetFlowUseCase
    )

  private fun assertMapPositionEquals(
    defaultMapPosition: MapPosition,
    emittedPosition: MapPosition
  ) {
    assertEquals(defaultMapPosition.latitude, emittedPosition.latitude)
    assertEquals(defaultMapPosition.longitude, emittedPosition.longitude)
    assertEquals(defaultMapPosition.zoom, emittedPosition.zoom)
    assertEquals(defaultMapPosition.orientation, emittedPosition.orientation)
    assertEquals(defaultMapPosition.label, emittedPosition.label)
  }
}
