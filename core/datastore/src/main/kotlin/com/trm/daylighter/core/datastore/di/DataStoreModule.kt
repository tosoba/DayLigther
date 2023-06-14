package com.trm.daylighter.core.datastore.di

import com.trm.daylighter.core.datastore.PreferenceDataSourceImpl
import com.trm.daylighter.core.domain.repo.PreferenceDataSource
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
interface DataStoreModule {
  @Binds fun PreferenceDataSourceImpl.preferenceRepo(): PreferenceDataSource
}
