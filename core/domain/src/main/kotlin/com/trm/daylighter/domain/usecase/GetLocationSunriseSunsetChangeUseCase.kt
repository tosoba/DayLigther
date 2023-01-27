package com.trm.daylighter.domain.usecase

import com.trm.daylighter.domain.model.*
import com.trm.daylighter.domain.repo.SunriseSunsetRepo
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onEach

class GetLocationSunriseSunsetChangeUseCase
@Inject
constructor(private val repo: SunriseSunsetRepo) {
  operator fun invoke(locationId: Long): Flow<Loadable<SunriseSunsetChange>> =
    flow {
        emit(LoadingFirst)
        try {
          emit(repo.getLocationSunriseSunsetChangeById(locationId).asLoadable())
        } catch (cancellationException: CancellationException) {
          throw cancellationException
        } catch (ex: Exception) {
          emit(FailedFirst(ex))
        }
      }
      .onEach { loadable -> if (loadable is Ready) repo.enqueueSync() }
}
