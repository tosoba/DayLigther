package com.trm.daylighter.core.domain.usecase

import com.trm.daylighter.core.domain.repo.LocationRepo
import com.trm.daylighter.core.domain.widget.WidgetManager
import javax.inject.Inject

class SaveLocationUseCase
@Inject
constructor(
  private val locationRepo: LocationRepo,
  private val widgetManager: WidgetManager,
  private val sendLocationSavedEventUseCase: SendLocationSavedEventUseCase
) {
  suspend operator fun invoke(latitude: Double, longitude: Double, name: String) {
    val location =
      locationRepo.saveLocation(latitude = latitude, longitude = longitude, name = name)
    if (location.isDefault) widgetManager.enqueueDefaultLocationWidgetsUpdate()
  }

  suspend operator fun invoke(id: Long, latitude: Double, longitude: Double, name: String) {
    val location =
      locationRepo.updateLocationLatLngById(
        id = id,
        latitude = latitude,
        longitude = longitude,
        name = name
      )
    if (location.isDefault) widgetManager.enqueueDefaultLocationWidgetsUpdate()
    sendLocationSavedEventUseCase(id)
  }
}
