package com.trm.daylighter.core.network.di

import com.trm.daylighter.core.network.DaylighterNetworkDataSource
import com.trm.daylighter.core.network.retrofit.DaylighterRetrofitDataSource
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
interface NetworkDataSourceModule {
  @Binds fun DaylighterRetrofitDataSource.binds(): DaylighterNetworkDataSource
}
