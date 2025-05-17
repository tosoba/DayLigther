package com.trm.daylighter.feature.location

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.Event
import app.cash.turbine.test
import com.trm.daylighter.core.common.model.MapDefaults
import com.trm.daylighter.core.domain.model.LatLng
import com.trm.daylighter.core.domain.model.Location
import com.trm.daylighter.core.domain.model.Ready
import com.trm.daylighter.core.domain.repo.GeocodingRepo
import com.trm.daylighter.core.domain.usecase.GetCurrentUserLatLngUseCase
import com.trm.daylighter.core.domain.usecase.GetGeocodingEmailFlowUseCase
import com.trm.daylighter.core.domain.usecase.GetLocationByIdUseCase
import com.trm.daylighter.core.domain.usecase.GetLocationDisplayNameUseCase
import com.trm.daylighter.core.domain.usecase.SaveLocationUseCase
import com.trm.daylighter.core.domain.usecase.SetGeocodingEmailUseCase
import com.trm.daylighter.core.testing.rule.MainDispatcherRule
import com.trm.daylighter.feature.location.model.LocationPreparedToSave
import com.trm.daylighter.feature.location.model.LocationScreenMode
import com.trm.daylighter.feature.location.model.MapPosition
import io.mockk.MockKStubScope
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import java.io.IOException
import java.time.LocalDateTime
import java.time.ZoneId
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.delay
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
          getLocationByIdUseCase =
            mockk<GetLocationByIdUseCase>().apply { coEvery { this@apply(any()) } returns null },
        )
        .screenMode,
    )
  }

  @Test
  fun `GIVEN no initial location id THEN map position flow should only emit default map position`() =
    runTest {
      viewModel().mapPositionFlow.test {
        runCurrent()
        assertMapPositionEquals(MapPosition(), awaitItem())
        ensureAllEventsConsumed()
      }
    }

  @Test
  fun `GIVEN an initial location id WHEN get location by id returns null THEN map position flow should only emit default map position`() =
    runTest {
      viewModel(
          savedStateHandle = SavedStateHandle(mapOf(locationIdParam to 33L)),
          getLocationByIdUseCase =
            mockk<GetLocationByIdUseCase>().apply { coEvery { this@apply(any()) } returns null },
        )
        .mapPositionFlow
        .test {
          runCurrent()
          assertMapPositionEquals(MapPosition(), awaitItem())
          ensureAllEventsConsumed()
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
          zoneId = ZoneId.systemDefault(),
        )

      viewModel(
          savedStateHandle = SavedStateHandle(mapOf(locationIdParam to 61L)),
          getLocationByIdUseCase =
            mockk<GetLocationByIdUseCase>().apply {
              coEvery { this@apply(any()) } returns expectedLocation
            },
        )
        .mapPositionFlow
        .test {
          runCurrent()
          assertMapPositionEquals(
            MapPosition(
              latitude = expectedLocation.latitude,
              longitude = expectedLocation.longitude,
              zoom = MapDefaults.INITIAL_LOCATION_ZOOM,
              label = expectedLocation.name,
            ),
            awaitItem(),
          )
          ensureAllEventsConsumed()
        }
    }

  @Test
  fun `WHEN nothing is called THEN loading flow should emit false`() = runTest {
    viewModel().loadingFlow.test {
      runCurrent()
      assertEquals(false, awaitItem())
      ensureAllEventsConsumed()
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
            awaitItem(),
          )
          ensureAllEventsConsumed()
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
          ensureAllEventsConsumed()
        }
      }
    }

  @Test
  fun `WHEN request get and save user location is called THEN loading flow emits false, true, false`() =
    runTest {
      with(
        viewModel(
          getCurrentUserLatLngUseCase =
            mockk<GetCurrentUserLatLngUseCase>().apply {
              coEvery { this@apply() } returns LatLng(0.0, 0.0)
            }
        )
      ) {
        loadingFlow.test {
          runCurrent()
          requestGetAndSaveUserLocation()
          assertEquals(false, awaitItem())
          assertEquals(true, awaitItem())
          assertEquals(false, awaitItem())
          ensureAllEventsConsumed()
        }
      }
    }

  @Test
  fun `WHEN request get and save user location is called and location is found THEN location prepared to save flow first emits null and user location`() =
    runTest {
      val userLocation = LatLng(latitude = 10.0, longitude = 40.0)
      with(
        viewModel(
          getCurrentUserLatLngUseCase =
            mockk<GetCurrentUserLatLngUseCase>().apply {
              coEvery { this@apply() } returns userLocation
            }
        )
      ) {
        locationPreparedToSaveFlow.test {
          runCurrent()
          requestGetAndSaveUserLocation()
          assertEquals(null, awaitItem())
          assertEquals(
            LocationPreparedToSave(
              latitude = userLocation.latitude,
              longitude = userLocation.longitude,
              isUser = true,
            ),
            awaitItem(),
          )
          ensureAllEventsConsumed()
        }
      }
    }

  @Test
  fun `WHEN request get and save user location is called and location is found THEN map position is updated to found location`() =
    runTest {
      val userLocation = LatLng(latitude = 12.0, longitude = 48.0)
      with(
        viewModel(
          getCurrentUserLatLngUseCase =
            mockk<GetCurrentUserLatLngUseCase>().apply {
              coEvery { this@apply() } returns userLocation
            }
        )
      ) {
        locationPreparedToSaveFlow.test {
          runCurrent()
          requestGetAndSaveUserLocation()
          cancelAndIgnoreRemainingEvents()
          assertTrue {
            with(mapPositionFlow.value) {
              latitude == userLocation.latitude &&
                longitude == userLocation.longitude &&
                zoom == MapDefaults.INITIAL_LOCATION_ZOOM
            }
          }
        }
      }
    }

  @Test
  fun `WHEN request get and save user location is called and location is not found THEN user location not found flow first emits false, true, false`() =
    runTest {
      with(
        viewModel(
          getCurrentUserLatLngUseCase =
            mockk<GetCurrentUserLatLngUseCase>().apply { coEvery { this@apply() } returns null }
        )
      ) {
        userLocationNotFoundFlow.test(timeout = 5_000.milliseconds) {
          runCurrent()
          requestGetAndSaveUserLocation()
          assertEquals(false, awaitItem())
          assertEquals(true, awaitItem())
          assertEquals(false, awaitItem())
          ensureAllEventsConsumed()
        }
      }
    }

  @Test
  fun `WHEN cancel current location request is called THEN loading flow emits false twice`() =
    runTest {
      with(viewModel()) {
        loadingFlow.test {
          runCurrent()
          cancelCurrentSaveLocationRequest()
          val events = cancelAndConsumeRemainingEvents()
          assertEquals(2, events.size)
          assertTrue(events.all { it is Event.Item && !it.value })
        }
      }
    }

  @Test
  fun `WHEN cancel current location request is called THEN location prepared to save flow emits null`() =
    runTest {
      with(viewModel()) {
        locationPreparedToSaveFlow.test {
          runCurrent()
          cancelCurrentSaveLocationRequest()
          assertEquals(null, awaitItem())
          ensureAllEventsConsumed()
        }
      }
    }

  @Test
  fun `WHEN cancel current location request is called THEN user location not found flow emits false`() =
    runTest {
      with(viewModel()) {
        userLocationNotFoundFlow.test {
          runCurrent()
          cancelCurrentSaveLocationRequest()
          assertEquals(false, awaitItem())
          ensureAllEventsConsumed()
        }
      }
    }

  @Test
  fun `GIVEN a request get and save user location in progress WHEN cancel current location request is called before user location is found THEN location prepared to save flow emits null twice`() =
    runTest {
      with(
        viewModel(
          getCurrentUserLatLngUseCase =
            mockk<GetCurrentUserLatLngUseCase>().apply {
              coEvery { this@apply() } coAnswers
                {
                  delay(5_000L)
                  LatLng(0.0, 0.0)
                }
            }
        )
      ) {
        locationPreparedToSaveFlow.test {
          runCurrent()
          requestGetAndSaveUserLocation()
          delay(2_000L)
          cancelCurrentSaveLocationRequest()
          assertTrue {
            val events = cancelAndConsumeRemainingEvents()
            events.size == 2 && events.all { it is Event.Item && it.value == null }
          }
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
          ensureAllEventsConsumed()
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
          ensureAllEventsConsumed()
        }
      }
    }

  @Test
  fun `WHEN on map view pause is called THEN map position is saved`() = runTest {
    with(viewModel()) {
      val mapPosition = MapPosition(latitude = 15.0, longitude = 34.0, zoom = 7.0)
      onMapViewPause(mapPosition)
      mapPositionFlow.test {
        runCurrent()
        assertEquals(mapPosition, awaitItem())
        ensureAllEventsConsumed()
      }
    }
  }

  @Test
  fun `WHEN input location name is called THEN location name ready flow emits that name`() =
    runTest {
      with(viewModel()) {
        val locationName = "test name"
        locationNameReadyFlow.test {
          runCurrent()
          inputLocationName(locationName)
          assertEquals(locationName, awaitItem())
          ensureAllEventsConsumed()
        }
      }
    }

  @Test
  fun `WHEN input location name is called THEN location name loading flow emits false`() = runTest {
    with(viewModel()) {
      locationNameLoadingFlow.test {
        runCurrent()
        inputLocationName("test name")
        assertEquals(false, awaitItem())
        ensureAllEventsConsumed()
      }
    }
  }

  @Test
  fun `WHEN input location name is called THEN location name failure message flow emits null`() =
    runTest {
      with(viewModel()) {
        locationNameFailureMessageFlow.test {
          runCurrent()
          inputLocationName("test name")
          assertEquals(null, awaitItem())
          ensureAllEventsConsumed()
        }
      }
    }

  @Test
  fun `WHEN clear location name is called THEN location name ready flow emits empty string`() =
    runTest {
      with(viewModel()) {
        locationNameReadyFlow.test {
          runCurrent()
          clearLocationName()
          assertEquals("", awaitItem())
          ensureAllEventsConsumed()
        }
      }
    }

  @Test
  fun `WHEN clear location name is called THEN location name loading flow emits false`() = runTest {
    with(viewModel()) {
      locationNameLoadingFlow.test {
        runCurrent()
        clearLocationName()
        assertEquals(false, awaitItem())
        ensureAllEventsConsumed()
      }
    }
  }

  @Test
  fun `WHEN clear location name is called THEN location name failure message flow emits null`() =
    runTest {
      with(viewModel()) {
        locationNameFailureMessageFlow.test {
          runCurrent()
          clearLocationName()
          assertEquals(null, awaitItem())
          ensureAllEventsConsumed()
        }
      }
    }

  @Test
  fun `WHEN get location display name is called and location name is returned successfully THEN location name loading flow emits true, false`() =
    runTest {
      with(
        viewModel(
          getLocationDisplayNameUseCase = getLocationDisplayName { returns("geocoded name") }
        )
      ) {
        locationNameLoadingFlow.test {
          runCurrent()
          getLocationDisplayName(5.0, 16.0)
          assertEquals(true, awaitItem())
          assertEquals(false, awaitItem())
          ensureAllEventsConsumed()
        }
      }
    }

  @Test
  fun `WHEN get location display name is called and location name is null THEN location name loading flow emits true, false`() =
    runTest {
      with(viewModel(getLocationDisplayNameUseCase = getLocationDisplayName { returns(null) })) {
        locationNameLoadingFlow.test(timeout = 5_000.milliseconds) {
          runCurrent()
          getLocationDisplayName(5.0, 16.0)
          assertEquals(true, awaitItem())
          assertEquals(false, awaitItem())
          assertEquals(false, awaitItem())
          ensureAllEventsConsumed()
        }
      }
    }

  @Test
  fun `WHEN get location display name is called and exception is thrown THEN location name loading flow emits true, false, false`() =
    runTest {
      with(
        viewModel(getLocationDisplayNameUseCase = getLocationDisplayName { throws(IOException()) })
      ) {
        locationNameLoadingFlow.test(timeout = 5_000.milliseconds) {
          runCurrent()
          getLocationDisplayName(5.0, 16.0)
          assertEquals(true, awaitItem())
          assertEquals(false, awaitItem())
          assertEquals(false, awaitItem())
          ensureAllEventsConsumed()
        }
      }
    }

  @Test
  fun `WHEN get location display name is called and location name is returned successfully THEN location name ready flow emits empty string and returned name`() =
    runTest {
      val locationName = "geocoded name"
      with(
        viewModel(getLocationDisplayNameUseCase = getLocationDisplayName { returns(locationName) })
      ) {
        locationNameReadyFlow.test {
          runCurrent()
          getLocationDisplayName(5.0, 16.0)
          assertEquals("", awaitItem())
          assertEquals(locationName, awaitItem())
          ensureAllEventsConsumed()
        }
      }
    }

  @Test
  fun `WHEN get location display name is called and location name is null THEN location name ready flow emits empty string 3 times`() =
    runTest {
      with(viewModel(getLocationDisplayNameUseCase = getLocationDisplayName { returns(null) })) {
        locationNameReadyFlow.test(timeout = 5_000.milliseconds) {
          runCurrent()
          getLocationDisplayName(5.0, 16.0)
          repeat(3) { assertEquals("", awaitItem()) }
          ensureAllEventsConsumed()
        }
      }
    }

  @Test
  fun `WHEN get location display name is called and exception is thrown THEN location name ready flow emits empty string 3 times`() =
    runTest {
      with(
        viewModel(getLocationDisplayNameUseCase = getLocationDisplayName { throws(IOException()) })
      ) {
        locationNameReadyFlow.test(timeout = 5_000.milliseconds) {
          runCurrent()
          getLocationDisplayName(5.0, 16.0)
          repeat(3) { assertEquals("", awaitItem()) }
          ensureAllEventsConsumed()
        }
      }
    }

  @Test
  fun `WHEN get location display name is called and location name is returned successfully THEN location name failure message flow emits null twice`() =
    runTest {
      with(
        viewModel(
          getLocationDisplayNameUseCase = getLocationDisplayName { returns("geocoded name") }
        )
      ) {
        locationNameFailureMessageFlow.test {
          runCurrent()
          getLocationDisplayName(5.0, 16.0)
          val events = cancelAndConsumeRemainingEvents()
          assertEquals(2, events.size)
          assertTrue(events.all { it is Event.Item && it.value == null })
        }
      }
    }

  @Test
  fun `WHEN get location display name is called and location name is null THEN location name failure message flow emits null, location name not found, null`() =
    runTest {
      with(viewModel(getLocationDisplayNameUseCase = getLocationDisplayName { returns(null) })) {
        locationNameFailureMessageFlow.test(timeout = 5_000.milliseconds) {
          runCurrent()
          getLocationDisplayName(5.0, 16.0)
          assertEquals(null, awaitItem())
          assertEquals(R.string.location_name_not_found, awaitItem())
          assertEquals(null, awaitItem())
          ensureAllEventsConsumed()
        }
      }
    }

  @Test
  fun `WHEN get location display name is called and exception is thrown THEN location name failure message flow emits null, geocoding error, null`() =
    runTest {
      with(
        viewModel(getLocationDisplayNameUseCase = getLocationDisplayName { throws(Exception()) })
      ) {
        locationNameFailureMessageFlow.test(timeout = 5_000.milliseconds) {
          runCurrent()
          getLocationDisplayName(5.0, 16.0)
          assertEquals(null, awaitItem())
          assertEquals(R.string.geocoding_error, awaitItem())
          assertEquals(null, awaitItem())
          ensureAllEventsConsumed()
        }
      }
    }

  private fun getLocationDisplayName(repoResponse: MockKStubScope<String?, String?>.() -> Unit) =
    GetLocationDisplayNameUseCase(
      mockk<GeocodingRepo>().apply {
        coEvery { this@apply.getLocationDisplayName(any(), any()) }.repoResponse()
      }
    )

  private fun viewModel(
    savedStateHandle: SavedStateHandle = SavedStateHandle(),
    getLocationByIdUseCase: GetLocationByIdUseCase = mockk(),
    saveLocationUseCase: SaveLocationUseCase = mockk(),
    getCurrentUserLatLngUseCase: GetCurrentUserLatLngUseCase = mockk(),
    getLocationDisplayNameUseCase: GetLocationDisplayNameUseCase = mockk(),
    setGeocodingEmailUseCase: SetGeocodingEmailUseCase = mockk(),
    getGeocodingEmailFlowUseCase: GetGeocodingEmailFlowUseCase =
      mockk<GetGeocodingEmailFlowUseCase>().apply { every { this@apply() } returns flowOf(null) },
  ): LocationViewModel =
    LocationViewModel(
      savedStateHandle,
      getLocationByIdUseCase,
      saveLocationUseCase,
      getCurrentUserLatLngUseCase,
      getLocationDisplayNameUseCase,
      setGeocodingEmailUseCase,
      getGeocodingEmailFlowUseCase,
    )

  private fun assertMapPositionEquals(
    defaultMapPosition: MapPosition,
    emittedPosition: MapPosition,
  ) {
    assertEquals(defaultMapPosition.latitude, emittedPosition.latitude)
    assertEquals(defaultMapPosition.longitude, emittedPosition.longitude)
    assertEquals(defaultMapPosition.zoom, emittedPosition.zoom)
    assertEquals(defaultMapPosition.orientation, emittedPosition.orientation)
    assertEquals(defaultMapPosition.label, emittedPosition.label)
  }
}
