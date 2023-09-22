package com.trm.daylighter.feature.location

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.Event
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
import com.trm.daylighter.feature.location.model.LocationPreparedToSave
import com.trm.daylighter.feature.location.model.LocationScreenMode
import com.trm.daylighter.feature.location.model.MapPosition
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import java.time.LocalDateTime
import java.time.ZoneId
import kotlin.test.assertEquals
import kotlin.test.assertTrue
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
        assertTrue { cancelAndConsumeRemainingEvents().isEmpty() }
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
          assertTrue { cancelAndConsumeRemainingEvents().isEmpty() }
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
          assertTrue { cancelAndConsumeRemainingEvents().isEmpty() }
        }
    }

  @Test
  fun `WHEN nothing is called THEN loading flow should emit false`() = runTest {
    viewModel().loadingFlow.test {
      runCurrent()
      assertEquals(false, awaitItem())
      assertTrue { cancelAndConsumeRemainingEvents().isEmpty() }
    }
  }

  @Test
  fun `WHEN nothing is called THEN save location should emit no events`() = runTest {
    viewModel().locationSavedFlow.test {
      runCurrent()
      expectNoEvents()
    }
  }

  @Test
  fun `WHEN nothing is called THEN location prepared to save flow should emit no events`() =
    runTest {
      viewModel().locationPreparedToSaveFlow.test {
        runCurrent()
        expectNoEvents()
      }
    }

  @Test
  fun `WHEN nothing is called THEN user location not found flow should emit no events`() = runTest {
    viewModel().userLocationNotFoundFlow.test {
      runCurrent()
      expectNoEvents()
    }
  }

  @Test
  fun `WHEN request save specified location is called THEN loading flow should emit only false items`() =
    runTest {
      with(viewModel()) {
        loadingFlow.test {
          runCurrent()
          requestSaveSpecifiedLocation(0.0, 0.0)
          assertTrue { cancelAndConsumeRemainingEvents().all { it is Event.Item && !it.value } }
        }
      }
    }

  @Test
  fun `WHEN request save specified location is called THEN location prepared to save flow should emit a location prepared to save`() =
    runTest {
      with(viewModel()) {
        locationPreparedToSaveFlow.test {
          runCurrent()
          val latitude = 11.0
          val longitude = 22.0
          requestSaveSpecifiedLocation(latitude = latitude, longitude = longitude)
          assertEquals(
            LocationPreparedToSave(latitude = latitude, longitude = longitude, isUser = false),
            awaitItem()
          )
          assertTrue { cancelAndConsumeRemainingEvents().isEmpty() }
        }
      }
    }

  @Test
  fun `WHEN request save specified location is called THEN user location not found flow only emit false`() =
    runTest {
      with(viewModel()) {
        userLocationNotFoundFlow.test {
          runCurrent()
          requestSaveSpecifiedLocation(0.0, 0.0)
          assertEquals(false, awaitItem())
          assertTrue { cancelAndConsumeRemainingEvents().isEmpty() }
        }
      }
    }

  @Test
  fun `GIVEN no initial location id WHEN save location is called THEN location saved flow should emit Ready`() =
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
          assertTrue { cancelAndConsumeRemainingEvents().isEmpty() }
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
          assertTrue { cancelAndConsumeRemainingEvents().isEmpty() }
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
