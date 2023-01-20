package com.trm.daylighter.data.repo

import com.trm.daylighter.core.network.DaylighterNetworkDataSource
import com.trm.daylighter.database.dao.SunriseSunsetDao
import com.trm.daylighter.domain.repo.SunriseSunsetRepo
import javax.inject.Inject

class SunriseSunsetRepoImpl
@Inject
constructor(
  private val dao: SunriseSunsetDao,
  private val network: DaylighterNetworkDataSource,
) : SunriseSunsetRepo {
  override suspend fun sync() {}
}
