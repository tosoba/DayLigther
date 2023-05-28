package com.trm.daylighter.work.initializer

import android.content.Context
import androidx.startup.AppInitializer
import androidx.startup.Initializer
import androidx.work.*
import com.trm.daylighter.work.worker.WidgetUpdateWorker

class WidgetUpdateInitializer : Initializer<WidgetUpdateInitializer.Companion> {
  override fun create(context: Context): Companion {
    WorkManager.getInstance(context).apply {
      enqueueUniquePeriodicWork(
        WidgetUpdateWorker.WORK_NAME,
        ExistingPeriodicWorkPolicy.KEEP,
        WidgetUpdateWorker.workRequest()
      )
    }

    return Companion
  }

  override fun dependencies(): List<Class<out Initializer<*>>> =
    listOf(WorkManagerInitializer::class.java)

  companion object {
    operator fun invoke(context: Context) {
      AppInitializer.getInstance(context).initializeComponent(WidgetUpdateInitializer::class.java)
    }
  }
}
