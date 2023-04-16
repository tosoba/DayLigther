package com.trm.daylighter.widget.locations

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.appwidget.updateAll
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.trm.daylighter.core.common.di.DaylighterDispatchers
import com.trm.daylighter.core.common.di.Dispatcher
import com.trm.daylighter.core.domain.model.*
import com.trm.daylighter.core.domain.repo.SunriseSunsetRepo
import com.trm.daylighter.widget.WidgetWorkerManager
import com.trm.daylighter.widget.util.ext.getGlanceIds
import com.trm.daylighter.work.worker.delegatedData
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

@HiltWorker
class LocationsSunriseSunsetWidgetWorker
@AssistedInject
constructor(
  @Assisted private val context: Context,
  @Assisted workerParameters: WorkerParameters,
  @Dispatcher(DaylighterDispatchers.IO) private val ioDispatcher: CoroutineDispatcher,
  private val sunriseSunsetRepo: SunriseSunsetRepo,
) : CoroutineWorker(context, workerParameters) {
  override suspend fun doWork(): Result {
    val glanceIds = context.getGlanceIds<LocationsSunriseSunsetWidget>()
    setWidgetState(glanceIds = glanceIds, newState = LoadingFirst)
    try {
      setWidgetState(
        glanceIds = glanceIds,
        newState =
          withContext(ioDispatcher) {
              listOfNotNull(sunriseSunsetRepo.getDefaultLocationSunriseSunsetChange()) // TODO:
            }
            .asLoadable()
      )
    } catch (ex: Exception) {
      setWidgetState(glanceIds = glanceIds, newState = FailedFirst(ex))
    }
    return Result.success()
  }

  private suspend fun setWidgetState(
    glanceIds: List<GlanceId>,
    newState: Loadable<List<LocationSunriseSunsetChange>>
  ) {
    glanceIds.forEach { glanceId ->
      updateAppWidgetState(
        context = context,
        definition = LocationsSunriseSunsetWidgetStateDefinition,
        glanceId = glanceId,
        updateState = { newState }
      )
    }
    LocationsSunriseSunsetWidget().updateAll(context)
  }

  internal companion object : WidgetWorkerManager() {
    override val workName: String = "LocationsSunriseSunsetWidgetWork"

    override val inputData: Data
      get() = LocationsSunriseSunsetWidgetWorker::class.delegatedData()
  }
}
