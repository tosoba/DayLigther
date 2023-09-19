package com.trm.daylighter.feature.location

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.trm.daylighter.core.common.model.MapDefaults
import com.trm.daylighter.core.domain.model.Location
import com.trm.daylighter.core.domain.model.Ready
import com.trm.daylighter.core.domain.usecase.GetCurrentUserLatLngUseCase
import com.trm.daylighter.core.domain.usecase.GetLocationById
import com.trm.daylighter.core.domain.usecase.GetLocationDisplayName
import com.trm.daylighter.core.domain.usecase.IsGeocodingEmailPreferenceSetFlowUseCase
import com.trm.daylighter.core.domain.usecase.SaveLocationUseCase
import com.trm.daylighter.core.testing.rule.MainDispatcherRule
import com.trm.daylighter.feature.location.model.LocationScreenMode
import com.trm.daylighter.feature.location.model.MapPosition
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import java.time.LocalDateTime
import java.time.ZoneId
import kotlin.test.assertEquals
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class LocationViewModelTests {
  @get:Rule val mainDispatcherRule = MainDispatcherRule()

  @Test
  fun `GIVEN no initial location id THEN screen mode is add`() {
    assertEquals(LocationScreenMode.ADD, viewModel().screenMode)
  }

  @Test
  fun `GIVEN an initial location id THEN screen mode is edit`() {
    assertEquals(
      LocationScreenMode.EDIT,
      viewModel(
          savedStateHandle = SavedStateHandle(mapOf(locationIdParam to 41L)),
          getLocationById =
            mockk<GetLocationById>().apply { coEvery { this@apply(any()) } returns null }
        )
        .screenMode
    )
  }

  @Test
  fun `GIVEN no initial location id THEN map position flow should only emit default map position`() =
    runTest {
      viewModel().mapPositionFlow.test {
        runCurrent()
        assertMapPositionEquals(MapPosition(), awaitItem())
        cancelAndIgnoreRemainingEvents()
      }
    }

  @Test
  fun `GIVEN an initial location id WHEN get location by id returns null THEN map position flow should only emit default map position`() =
    runTest {
      viewModel(
          savedStateHandle = SavedStateHandle(mapOf(locationIdParam to 33L)),
          getLocationById =
            mockk<GetLocationById>().apply { coEvery { this@apply(any()) } returns null }
        )
        .mapPositionFlow
        .test {
          runCurrent()
          assertMapPositionEquals(MapPosition(), awaitItem())
          cancelAndIgnoreRemainingEvents()
        }
    }

  @Test
  fun `GIVEN an initial location id THEN map position flow should map position for location id`() =
    runTest {
      val expectedLocation =
        Location(
          latitude = 0.0,
          longitude = 0.0,
          name = "",
          isDefault = false,
          updatedAt = LocalDateTime.now(),
          zoneId = ZoneId.systemDefault()
        )

      viewModel(
          savedStateHandle = SavedStateHandle(mapOf(locationIdParam to 61L)),
          getLocationById =
            mockk<GetLocationById>().apply {
              coEvery { this@apply(any()) } returns expectedLocation
            }
        )
        .mapPositionFlow
        .test {
          runCurrent()
          assertMapPositionEquals(
            MapPosition(
              latitude = expectedLocation.latitude,
              longitude = expectedLocation.longitude,
              zoom = MapDefaults.INITIAL_LOCATION_ZOOM,
              label = expectedLocation.name
            ),
            awaitItem()
          )
          cancelAndIgnoreRemainingEvents()
        }
    }

  @Test
  fun `WHEN nothing is called THEN loading flow should emit false`() = runTest {
    viewModel().loadingFlow.test {
      runCurrent()
      assertEquals(false, awaitItem())
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `WHEN nothing is called THEN save location should emit false`() = runTest {
    viewModel().locationSavedFlow.test {
      runCurrent()
      expectNoEvents()
    }
  }

  @Test
  fun `GIVEN no initial location id WHEN save location is called THEN save location flow should emit Ready`() =
    runTest {
      with(
        viewModel(
          saveLocationUseCase =
            mockk<SaveLocationUseCase>().apply {
              coEvery { this@apply(any(), any(), any()) } returns Unit
            }
        )
      ) {
        locationSavedFlow.test {
          runCurrent()
          saveLocation(0.0, 0.0, "")
          assertEquals(Ready(Unit), awaitItem())
          cancelAndIgnoreRemainingEvents()
        }
      }
    }

  @Test
  fun `GIVEN no initial location id WHEN save location is called THEN loading flow should emit false, true, false`() =
    runTest {
      with(
        viewModel(
          saveLocationUseCase =
            mockk<SaveLocationUseCase>().apply {
              coEvery { this@apply(any(), any(), any()) } returns Unit
            }
        )
      ) {
        loadingFlow.test {
          runCurrent()
          saveLocation(0.0, 0.0, "")
          assertEquals(false, awaitItem())
          assertEquals(true, awaitItem())
          assertEquals(false, awaitItem())
          cancelAndIgnoreRemainingEvents()
        }
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
