package com.trm.daylighter.work.di

import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequest
import androidx.work.PeriodicWorkRequestBuilder
import com.trm.daylighter.work.worker.DelegatingWorker
import com.trm.daylighter.work.worker.SyncWorker
import com.trm.daylighter.work.worker.delegatedData
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import java.time.Duration

@Module
@InstallIn(SingletonComponent::class)
class WorkModule {
  @Provides
  fun syncWorkRequest(): PeriodicWorkRequest =
    PeriodicWorkRequestBuilder<DelegatingWorker>(Duration.ofHours(1L))
      .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
      .setInputData(SyncWorker::class.delegatedData())
      .build()
}
