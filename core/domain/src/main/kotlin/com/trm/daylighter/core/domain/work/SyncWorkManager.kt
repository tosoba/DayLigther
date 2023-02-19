package com.trm.daylighter.core.domain.work

interface SyncWorkManager {
  fun enqueueSync()
  fun cancelSync()
}
