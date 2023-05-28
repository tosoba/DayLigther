package com.trm.daylighter.work.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.*
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
    widgetManager.updateDefaultLocationWidgets()
    return Result.success()
  }

  internal companion object {
    fun workRequest(): PeriodicWorkRequest =
      PeriodicWorkRequestBuilder<DelegatingWorker>(Duration.ofMinutes(1L))
        .setInputData(WidgetUpdateWorker::class.delegatedData())
        .build()

    const val WORK_NAME = "WidgetUpdateWork"
  }
}
