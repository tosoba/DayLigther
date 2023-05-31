package com.trm.daylighter.core.domain.usecase

import com.trm.daylighter.core.domain.di.DaylighterDispatchers
import com.trm.daylighter.core.domain.di.Dispatcher
import com.trm.daylighter.core.domain.model.*
import com.trm.daylighter.core.domain.repo.LocationRepo
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map

class GetLocationSunriseSunsetChangeAtIndexFlowUseCase
@Inject
constructor(
  private val calculateSunriseSunsetChangeUseCase: CalculateSunriseSunsetChangeUseCase,
  private val repo: LocationRepo,
  @Dispatcher(DaylighterDispatchers.IO) private val ioDispatcher: CoroutineDispatcher
) {
  operator fun invoke(index: Int): Flow<Loadable<LocationSunriseSunsetChange>> = flow {
    emit(LoadingFirst)
    try {
      emitAll(
        repo
          .getLocationAtOffsetFlow(offset = index)
          .map { location ->
            location.asLoadable().map(calculateSunriseSunsetChangeUseCase::invoke)
          }
          .flowOn(ioDispatcher)
      )
    } catch (ex: CancellationException) {
      throw ex
    } catch (ex: Exception) {
      emit(FailedFirst(ex))
    }
  }
}
