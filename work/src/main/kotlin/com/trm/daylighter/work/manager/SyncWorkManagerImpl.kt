package com.trm.daylighter.work.manager

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.WorkManager
import com.trm.daylighter.core.domain.work.SyncWorkManager
import com.trm.daylighter.work.worker.SyncWorker
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class SyncWorkManagerImpl
@Inject
constructor(
  @ApplicationContext private val context: Context,
) : SyncWorkManager {
  override fun enqueueSync() {
    WorkManager.getInstance(context)
      .enqueueUniquePeriodicWork(
        SyncWorker.WORK_NAME,
        ExistingPeriodicWorkPolicy.KEEP,
        SyncWorker.syncWorkRequest()
      )
  }

  override fun cancelSync() {
    WorkManager.getInstance(context).cancelUniqueWork(SyncWorker.WORK_NAME)
  }
}
