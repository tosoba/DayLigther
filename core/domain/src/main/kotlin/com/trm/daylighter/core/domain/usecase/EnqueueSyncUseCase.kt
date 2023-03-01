package com.trm.daylighter.core.domain.usecase

import com.trm.daylighter.core.domain.work.SyncWorkManager
import javax.inject.Inject

class EnqueueSyncUseCase @Inject constructor(private val manager: SyncWorkManager) {
  operator fun invoke() {
    manager.enqueueSync()
  }
}
