package com.trm.daylighter.work.initializer

import android.content.Context
import androidx.startup.AppInitializer
import androidx.startup.Initializer
import androidx.work.*
import com.trm.daylighter.work.worker.CleanupWorker

object Cleanup {
  fun initialize(context: Context) {
    AppInitializer.getInstance(context).initializeComponent(CleanupInitializer::class.java)
  }
}

internal const val CleanupWorkName = "CleanupWorkName"

class CleanupInitializer : Initializer<Cleanup> {
  override fun create(context: Context): Cleanup {
    WorkManager.getInstance(context).apply {
      enqueueUniquePeriodicWork(
        CleanupWorkName,
        ExistingPeriodicWorkPolicy.KEEP,
        CleanupWorker.workRequest()
      )
    }

    return Cleanup
  }

  override fun dependencies(): List<Class<out Initializer<*>>> =
    listOf(WorkManagerInitializer::class.java)
}
