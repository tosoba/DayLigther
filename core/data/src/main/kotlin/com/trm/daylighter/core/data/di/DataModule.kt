package com.trm.daylighter.core.data.di

import com.trm.daylighter.core.data.repo.GeocodingRepoImpl
import com.trm.daylighter.core.data.repo.LocationRepoImpl
import com.trm.daylighter.core.domain.repo.GeocodingRepo
import com.trm.daylighter.core.domain.repo.LocationRepo
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
interface DataModule {
  @Binds fun LocationRepoImpl.locationRepo(): LocationRepo

  @Binds fun GeocodingRepoImpl.geocodingRepo(): GeocodingRepo
}
