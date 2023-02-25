package com.trm.daylighter.core.domain.usecase

import com.trm.daylighter.core.domain.model.*
import com.trm.daylighter.core.domain.repo.SunriseSunsetRepo
import com.trm.daylighter.core.domain.work.SyncWorkManager
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onEach

class GetLocationSunriseSunsetChangeAtIndexUseCase
@Inject
constructor(private val repo: SunriseSunsetRepo, private val manager: SyncWorkManager) {
  operator fun invoke(index: Int): Flow<Loadable<LocationSunriseSunsetChange>> =
    flow {
        emit(LoadingFirst)
        try {
          emit(repo.getLocationSunriseSunsetChangeAtIndex(index).asLoadable())
        } catch (cancellationException: CancellationException) {
          throw cancellationException
        } catch (ex: Exception) {
          emit(FailedFirst(ex))
        }
      }
      .onEach { loadable -> if (loadable is Ready) manager.enqueueSync() }
}
