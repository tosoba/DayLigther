package com.trm.daylighter.sync.di

import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequest
import androidx.work.PeriodicWorkRequestBuilder
import com.trm.daylighter.sync.worker.DelegatingWorker
import com.trm.daylighter.sync.worker.SyncWorker
import com.trm.daylighter.sync.worker.delegatedData
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import java.time.Duration

@Module
@InstallIn(SingletonComponent::class)
class SyncModule {
  @Provides
  fun syncWorkRequest(): PeriodicWorkRequest =
    PeriodicWorkRequestBuilder<DelegatingWorker>(Duration.ofHours(1L))
      .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
      .setInputData(SyncWorker::class.delegatedData())
      .build()
}
