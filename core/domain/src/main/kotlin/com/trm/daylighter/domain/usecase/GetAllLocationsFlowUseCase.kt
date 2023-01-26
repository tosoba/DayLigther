package com.trm.daylighter.domain.usecase

import com.trm.daylighter.domain.model.Loadable
import com.trm.daylighter.domain.model.LoadingFirst
import com.trm.daylighter.domain.model.Location
import com.trm.daylighter.domain.model.asLoadable
import com.trm.daylighter.domain.repo.LocationRepo
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart

class GetAllLocationsFlowUseCase @Inject constructor(private val repo: LocationRepo) {
  operator fun invoke(): Flow<Loadable<List<Location>>> =
    repo.getAllFlow().map(List<Location>::asLoadable).onStart { emit(LoadingFirst) }
}
