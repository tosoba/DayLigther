package com.trm.daylighter.core.domain.usecase

import com.trm.daylighter.core.domain.repo.LocationRepo
import com.trm.daylighter.core.domain.widget.WidgetManager
import javax.inject.Inject

class SetDefaultLocationUseCase
@Inject
constructor(private val repo: LocationRepo, private val widgetManager: WidgetManager) {
  suspend operator fun invoke(id: Long) {
    repo.setDefaultLocationById(id)
    widgetManager.enqueueDefaultLocationWidgetsUpdate()
  }
}
