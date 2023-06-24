package com.trm.daylighter.core.domain.usecase

import com.trm.daylighter.core.domain.di.DayLighterDispatchers
import com.trm.daylighter.core.domain.di.Dispatcher
import com.trm.daylighter.core.domain.repo.LocationRepo
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

class GetNonDefaultLocationOffsetByIdUseCase
@Inject
constructor(
  private val repo: LocationRepo,
  @Dispatcher(DayLighterDispatchers.IO) private val ioDispatcher: CoroutineDispatcher
) {
  suspend operator fun invoke(id: Long): Int? =
    withContext(ioDispatcher) { repo.getNonDefaultLocationOffsetById(id) }
}
