package com.trm.daylighter.widget

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.appwidget.updateAll
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.trm.daylighter.core.common.di.DaylighterDispatchers
import com.trm.daylighter.core.common.di.Dispatcher
import com.trm.daylighter.core.domain.model.*
import com.trm.daylighter.core.domain.repo.SunriseSunsetRepo
import com.trm.daylighter.work.worker.DelegatingWorker
import com.trm.daylighter.work.worker.delegatedData
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn

@HiltWorker
class SunriseSunsetWidgetWorker
@AssistedInject
constructor(
  @Assisted private val context: Context,
  @Assisted workerParameters: WorkerParameters,
  @Dispatcher(DaylighterDispatchers.IO) private val ioDispatcher: CoroutineDispatcher,
  private val sunriseSunsetRepo: SunriseSunsetRepo,
) : CoroutineWorker(context, workerParameters) {
  override suspend fun doWork(): Result {
    val glanceIds = GlanceAppWidgetManager(context).getGlanceIds(SunriseSunsetWidget::class.java)
    sunriseSunsetRepo
      .getDefaultLocationSunriseSunsetChange()
      .distinctUntilChanged()
      .flowOn(ioDispatcher)
      .collect { setWidgetState(glanceIds = glanceIds, newState = it) }
    return Result.success()
  }

  private suspend fun setWidgetState(
    glanceIds: List<GlanceId>,
    newState: Loadable<LocationSunriseSunsetChange>
  ) {
    glanceIds.forEach { glanceId ->
      updateAppWidgetState(
        context = context,
        definition = SunriseSunsetWidgetStateDefinition,
        glanceId = glanceId,
        updateState = { newState }
      )
    }
    SunriseSunsetWidget().updateAll(context)
  }

  internal companion object {
    private const val WORK_NAME = "SunriseSunsetWidgetWork"

    fun enqueue(context: Context) {
      val manager = WorkManager.getInstance(context)
      manager.enqueueUniqueWork(
        WORK_NAME,
        ExistingWorkPolicy.KEEP,
        OneTimeWorkRequestBuilder<DelegatingWorker>()
          .setInputData(SunriseSunsetWidgetWorker::class.delegatedData())
          .build()
      )
    }

    fun cancel(context: Context) {
      WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
    }
  }
}
