package com.trm.daylighter.work.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.PeriodicWorkRequest
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkerParameters
import com.trm.daylighter.core.domain.widget.WidgetManager
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.time.Duration

@HiltWorker
class WidgetUpdateWorker
@AssistedInject
constructor(
  @Assisted private val appContext: Context,
  @Assisted workerParams: WorkerParameters,
  private val widgetManager: WidgetManager,
) : CoroutineWorker(appContext, workerParams) {
  override suspend fun doWork(): Result {
    widgetManager.updateAllLocationWidgets()
    return Result.success()
  }

  internal companion object {
    fun workRequest(): PeriodicWorkRequest =
      PeriodicWorkRequestBuilder<DelegatingWorker>(
          Duration.ofMillis(PeriodicWorkRequest.MIN_PERIODIC_INTERVAL_MILLIS)
        )
        .setInputData(WidgetUpdateWorker::class.delegatedData())
        .build()

    const val WORK_NAME = "WidgetUpdateWork"
  }
}
