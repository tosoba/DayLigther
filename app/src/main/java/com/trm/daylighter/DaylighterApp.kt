package com.trm.daylighter

import android.app.Application
import com.trm.daylighter.sync.initializer.Cleanup
import dagger.hilt.android.HiltAndroidApp
import org.osmdroid.config.Configuration

@HiltAndroidApp
class DaylighterApp : Application() {
  override fun onCreate() {
    super.onCreate()
    initializeOsm()
    initializeCleanup()
  }

  private fun initializeOsm() {
    Configuration.getInstance().userAgentValue = packageName
  }

  private fun initializeCleanup() {
    Cleanup.initialize(this)
  }
}
