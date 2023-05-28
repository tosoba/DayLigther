package com.trm.daylighter

import android.app.Application
import com.trm.daylighter.work.initializer.WidgetUpdateInitializer
import dagger.hilt.android.HiltAndroidApp
import org.osmdroid.config.Configuration
import timber.log.Timber

@HiltAndroidApp
class DaylighterApp : Application() {
  override fun onCreate() {
    super.onCreate()
    initializeTimber()
    initializeOsm()
    initializeWidgetUpdate()
  }

  private fun initializeTimber() {
    Timber.plant(Timber.DebugTree())
  }

  private fun initializeOsm() {
    Configuration.getInstance().userAgentValue = packageName
  }

  private fun initializeWidgetUpdate() {
    WidgetUpdateInitializer(this)
  }
}
