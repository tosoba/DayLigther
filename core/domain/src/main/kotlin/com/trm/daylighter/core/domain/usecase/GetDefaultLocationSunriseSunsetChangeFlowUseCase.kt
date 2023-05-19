package com.trm.daylighter.core.domain.usecase

import com.trm.daylighter.core.domain.di.DaylighterDispatchers
import com.trm.daylighter.core.domain.di.Dispatcher
import com.trm.daylighter.core.domain.model.FailedFirst
import com.trm.daylighter.core.domain.model.Loadable
import com.trm.daylighter.core.domain.model.LoadingFirst
import com.trm.daylighter.core.domain.model.LocationSunriseSunsetChange
import com.trm.daylighter.core.domain.model.asLoadable
import com.trm.daylighter.core.domain.repo.SunriseSunsetRepo
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext

class GetDefaultLocationSunriseSunsetChangeFlowUseCase
@Inject
constructor(
  private val repo: SunriseSunsetRepo,
  @Dispatcher(DaylighterDispatchers.IO) private val ioDispatcher: CoroutineDispatcher
) {
  operator fun invoke(): Flow<Loadable<LocationSunriseSunsetChange>> = flow {
    emit(LoadingFirst)
    try {
      emit(withContext(ioDispatcher) { repo.getDefaultLocationSunriseSunsetChange() }.asLoadable())
    } catch (ex: Exception) {
      emit(FailedFirst(ex))
    }
  }
}
