package com.trm.daylighter.work.di

import com.trm.daylighter.core.domain.work.SyncWorkManager
import com.trm.daylighter.work.manager.SyncWorkManagerImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
interface WorkModule {
  @Binds fun SyncWorkManagerImpl.binds(): SyncWorkManager
}
