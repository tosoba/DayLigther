package com.trm.daylighter.core.domain.usecase

import com.trm.daylighter.core.domain.di.DaylighterDispatchers
import com.trm.daylighter.core.domain.di.Dispatcher
import com.trm.daylighter.core.domain.model.FailedFirst
import com.trm.daylighter.core.domain.model.Loadable
import com.trm.daylighter.core.domain.model.LoadingFirst
import com.trm.daylighter.core.domain.model.LocationSunriseSunsetChange
import com.trm.daylighter.core.domain.model.asLoadable
import com.trm.daylighter.core.domain.repo.LocationRepo
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext

class GetDefaultLocationSunriseSunsetChangeFlowUseCase
@Inject
constructor(
  private val calculateSunriseSunsetChangeUseCase: CalculateSunriseSunsetChangeUseCase,
  private val repo: LocationRepo,
  @Dispatcher(DaylighterDispatchers.IO) private val ioDispatcher: CoroutineDispatcher
) {
  operator fun invoke(): Flow<Loadable<LocationSunriseSunsetChange>> = flow {
    emit(LoadingFirst)
    try {
      emit(
        withContext(ioDispatcher) { repo.getDefaultLocation() }
          ?.let(calculateSunriseSunsetChangeUseCase::invoke)
          .asLoadable()
      )
    } catch (ex: CancellationException) {
      throw ex
    } catch (ex: Exception) {
      emit(FailedFirst(ex))
    }
  }
}
