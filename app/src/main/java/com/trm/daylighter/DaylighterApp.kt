package com.trm.daylighter

import android.app.Application
import com.trm.daylighter.sync.initializer.Sync
import dagger.hilt.android.HiltAndroidApp
import org.osmdroid.config.Configuration

@HiltAndroidApp
class DaylighterApp : Application() {
  override fun onCreate() {
    super.onCreate()
    Sync.initialize(context = this)
    initializeOsm()
  }

  private fun initializeOsm() {
    Configuration.getInstance().userAgentValue = packageName
  }
}
