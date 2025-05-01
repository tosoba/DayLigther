package com.trm.daylighter.core.domain.usecase

import com.trm.daylighter.core.domain.exception.LocationDisplayNameNotFound
import com.trm.daylighter.core.domain.model.FailedFirst
import com.trm.daylighter.core.domain.model.Loadable
import com.trm.daylighter.core.domain.model.LoadingFirst
import com.trm.daylighter.core.domain.model.Ready
import com.trm.daylighter.core.domain.repo.GeocodingRepo
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class GetLocationDisplayNameUseCase @Inject constructor(private val geocodingRepo: GeocodingRepo) {
  operator fun invoke(lat: Double, lng: Double): Flow<Loadable<String>> = flow {
    emit(LoadingFirst)
    try {
      geocodingRepo.getLocationDisplayName(lat = lat, lng = lng)?.let { displayName ->
        emit(Ready(displayName))
      } ?: run { emit(FailedFirst(LocationDisplayNameNotFound)) }
    } catch (ex: CancellationException) {
      throw ex
    } catch (ex: Exception) {
      emit(FailedFirst(ex))
    }
  }
}
