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
class CleanupWorker
@AssistedInject
constructor(
  @Assisted private val appContext: Context,
  @Assisted workerParams: WorkerParameters,
  @Dispatcher(DaylighterDispatchers.IO) private val ioDispatcher: CoroutineDispatcher,
  private val repo: SunriseSunsetRepo,
) : CoroutineWorker(appContext, workerParams) {
  override suspend fun doWork(): Result =
    withContext(ioDispatcher) {
      repo.deleteForEachLocationExceptMostRecent(limit = 2)
      Result.success()
    }

  companion object {
    fun workRequest(): PeriodicWorkRequest =
      PeriodicWorkRequestBuilder<DelegatingWorker>(Duration.ofDays(1L))
        .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
        .setInputData(CleanupWorker::class.delegatedData())
        .build()
  }
}
