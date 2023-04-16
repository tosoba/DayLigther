package com.trm.daylighter.widget

import android.content.Context
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.trm.daylighter.work.worker.DelegatingWorker

abstract class WidgetWorkerManager {
  abstract val workName: String
  abstract val inputData: Data

  fun enqueue(context: Context) {
    WorkManager.getInstance(context)
      .enqueueUniqueWork(
        workName,
        ExistingWorkPolicy.KEEP,
        OneTimeWorkRequestBuilder<DelegatingWorker>().setInputData(inputData).build()
      )
  }

  fun cancel(context: Context) {
    WorkManager.getInstance(context).cancelUniqueWork(workName)
  }
}
