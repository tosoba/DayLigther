package com.trm.daylighter.work.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.trm.daylighter.core.common.di.DaylighterDispatchers
import com.trm.daylighter.core.common.di.Dispatcher
import com.trm.daylighter.core.domain.repo.SunriseSunsetRepo
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.time.Duration
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

@HiltWorker
class SyncWorker
@AssistedInject
constructor(
  @Assisted private val appContext: Context,
  @Assisted workerParams: WorkerParameters,
  @Dispatcher(DaylighterDispatchers.IO) private val ioDispatcher: CoroutineDispatcher,
  private val repo: SunriseSunsetRepo,
) : CoroutineWorker(appContext, workerParams) {
  override suspend fun doWork(): Result =
    withContext(ioDispatcher) { if (repo.sync()) Result.success() else Result.retry() }

  internal companion object {
    fun syncWorkRequest(): PeriodicWorkRequest =
      PeriodicWorkRequestBuilder<DelegatingWorker>(Duration.ofHours(1L))
        .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
        .setInputData(SyncWorker::class.delegatedData())
        .build()

    const val WORK_NAME = "SyncWork"
  }
}
