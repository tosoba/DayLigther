package com.trm.daylighter.core.domain.usecase

import com.trm.daylighter.core.domain.model.Location
import com.trm.daylighter.core.domain.repo.LocationRepo
import com.trm.daylighter.core.domain.widget.WidgetManager
import javax.inject.Inject

class DeleteLocationUseCase
@Inject
constructor(private val locationRepo: LocationRepo, private val widgetManager: WidgetManager) {
  suspend operator fun invoke(location: Location) {
    locationRepo.deleteLocationById(id = location.id, isDefault = location.isDefault)
    widgetManager.updateAllLocationWidgets()
  }
}
