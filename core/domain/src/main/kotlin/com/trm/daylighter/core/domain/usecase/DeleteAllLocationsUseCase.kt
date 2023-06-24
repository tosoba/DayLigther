package com.trm.daylighter.core.domain.usecase

import com.trm.daylighter.core.domain.repo.LocationRepo
import com.trm.daylighter.core.domain.widget.WidgetManager
import javax.inject.Inject

class DeleteAllLocationsUseCase
@Inject
constructor(private val locationRepo: LocationRepo, private val widgetManager: WidgetManager) {
  suspend operator fun invoke() {
    locationRepo.deleteAllLocations()
    widgetManager.updateAllLocationWidgets()
  }
}
