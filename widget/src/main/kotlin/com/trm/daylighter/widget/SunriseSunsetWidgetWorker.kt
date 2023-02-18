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
import com.trm.daylighter.core.domain.model.Loadable
import com.trm.daylighter.core.domain.model.LoadingFirst
import com.trm.daylighter.core.domain.model.Location
import com.trm.daylighter.core.domain.model.asLoadable
import com.trm.daylighter.core.domain.repo.LocationRepo
import com.trm.daylighter.work.worker.DelegatingWorker
import com.trm.daylighter.work.worker.delegatedData
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart

@HiltWorker
class SunriseSunsetWidgetWorker
@AssistedInject
constructor(
  @Assisted private val context: Context,
  @Assisted workerParameters: WorkerParameters,
  @Dispatcher(DaylighterDispatchers.IO) private val ioDispatcher: CoroutineDispatcher,
  private val locationRepo: LocationRepo,
) : CoroutineWorker(context, workerParameters) {
  override suspend fun doWork(): Result {
    val glanceIds = GlanceAppWidgetManager(context).getGlanceIds(SunriseSunsetWidget::class.java)
    locationRepo
      .getDefaultLocationFlow()
      .flowOn(ioDispatcher)
      .map(Location?::asLoadable)
      .onStart { emit(LoadingFirst) }
      .collect { setWidgetState(glanceIds = glanceIds, newState = it) }
    return Result.success()
  }

  private suspend fun setWidgetState(glanceIds: List<GlanceId>, newState: Loadable<Location>) {
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

  companion object {
    private val uniqueWorkName = SunriseSunsetWidget::class.java.simpleName

    fun enqueue(context: Context) {
      val manager = WorkManager.getInstance(context)
      manager.enqueueUniqueWork(
        uniqueWorkName,
        ExistingWorkPolicy.KEEP,
        OneTimeWorkRequestBuilder<DelegatingWorker>()
          .setInputData(SunriseSunsetWidgetWorker::class.delegatedData())
          .build()
      )
    }

    fun cancel(context: Context) {
      WorkManager.getInstance(context).cancelUniqueWork(uniqueWorkName)
    }
  }
}
