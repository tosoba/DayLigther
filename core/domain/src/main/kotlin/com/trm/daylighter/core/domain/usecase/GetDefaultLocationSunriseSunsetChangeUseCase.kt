package com.trm.daylighter.core.domain.usecase

import com.trm.daylighter.core.domain.di.DayLighterDispatchers
import com.trm.daylighter.core.domain.di.Dispatcher
import com.trm.daylighter.core.domain.model.FailedFirst
import com.trm.daylighter.core.domain.model.Loadable
import com.trm.daylighter.core.domain.model.LocationSunriseSunsetChange
import com.trm.daylighter.core.domain.model.asLoadable
import com.trm.daylighter.core.domain.repo.LocationRepo
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

class GetDefaultLocationSunriseSunsetChangeUseCase
@Inject
constructor(
  private val calculateLocationSunriseSunsetChangeUseCase:
    CalculateLocationSunriseSunsetChangeUseCase,
  private val repo: LocationRepo,
  @Dispatcher(DayLighterDispatchers.IO) private val ioDispatcher: CoroutineDispatcher
) {
  suspend operator fun invoke(): Loadable<LocationSunriseSunsetChange> =
    try {
      withContext(ioDispatcher) { repo.getDefaultLocation() }
        ?.let(calculateLocationSunriseSunsetChangeUseCase::invoke)
        .asLoadable()
    } catch (ex: CancellationException) {
      throw ex
    } catch (ex: Exception) {
      FailedFirst(ex)
    }
}
