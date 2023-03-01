package com.trm.daylighter.core.domain.usecase

import com.trm.daylighter.core.domain.model.*
import com.trm.daylighter.core.domain.repo.SunriseSunsetRepo
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class GetLocationSunriseSunsetChangeAtIndexUseCase
@Inject
constructor(private val repo: SunriseSunsetRepo) {
  operator fun invoke(index: Int): Flow<Loadable<LocationSunriseSunsetChange>> = flow {
    emit(LoadingFirst)
    try {
      emit(repo.getLocationSunriseSunsetChangeAtIndex(index).asLoadable())
    } catch (ex: CancellationException) {
      throw ex
    } catch (ex: Exception) {
      emit(FailedFirst(ex))
    }
  }
}
