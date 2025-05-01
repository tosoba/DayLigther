package com.trm.daylighter.core.domain.usecase

import com.trm.daylighter.core.domain.di.DayLighterDispatchers
import com.trm.daylighter.core.domain.di.Dispatcher
import com.trm.daylighter.core.domain.model.Loadable
import com.trm.daylighter.core.domain.model.LocationSunriseSunsetChange
import com.trm.daylighter.core.domain.model.asLoadable
import com.trm.daylighter.core.domain.repo.LocationRepo
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

class GetLocationSunriseSunsetChangeByIdUseCase
@Inject
constructor(
  private val calculateLocationSunriseSunsetChangeUseCase:
    CalculateLocationSunriseSunsetChangeUseCase,
  private val repo: LocationRepo,
  @Dispatcher(DayLighterDispatchers.IO) private val ioDispatcher: CoroutineDispatcher,
) {
  suspend operator fun invoke(id: Long): Loadable<LocationSunriseSunsetChange> =
    withContext(ioDispatcher) { repo.getLocationById(id) ?: repo.getDefaultLocation() }
      ?.let(calculateLocationSunriseSunsetChangeUseCase::invoke)
      .asLoadable()
}
