package com.trm.daylighter.data.di

import com.trm.daylighter.data.repo.SunriseSunsetRepoImpl
import com.trm.daylighter.domain.repo.SunriseSunsetRepo
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
interface DataModule {
  @Binds fun SunriseSunsetRepoImpl.binds(): SunriseSunsetRepo
}
