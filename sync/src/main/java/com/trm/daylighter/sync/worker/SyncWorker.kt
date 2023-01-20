package com.trm.daylighter.sync.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.trm.daylighter.core.common.di.DaylighterDispatchers
import com.trm.daylighter.core.common.di.Dispatcher
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.time.Duration
import kotlinx.coroutines.CoroutineDispatcher

@HiltWorker
class SyncWorker
@AssistedInject
constructor(
  @Assisted private val appContext: Context,
  @Assisted workerParams: WorkerParameters,
  @Dispatcher(DaylighterDispatchers.IO) private val ioDispatcher: CoroutineDispatcher,
) : CoroutineWorker(appContext, workerParams) {
  override suspend fun doWork(): Result {
    return Result.success()
  }

  companion object {
    fun startUpSyncWork(): PeriodicWorkRequest =
      PeriodicWorkRequestBuilder<DelegatingWorker>(Duration.ofDays(1L))
        .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
        .setInputData(SyncWorker::class.delegatedData())
        .build()
  }
}
