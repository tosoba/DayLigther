package com.trm.daylighter

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import org.osmdroid.config.Configuration

@HiltAndroidApp
class DaylighterApp : Application() {
  override fun onCreate() {
    super.onCreate()
    initializeOsm()
  }

  private fun initializeOsm() {
    Configuration.getInstance().userAgentValue = packageName
  }
}
