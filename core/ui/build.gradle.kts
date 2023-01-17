plugins {
  id("daylighter.android.library")
  id("daylighter.android.library.compose")
}

android {
  android { namespace = "com.trm.daylighter.core.ui" }

  defaultConfig { testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner" }
}

dependencies {
  implementation(project(":core:domain"))

  implementation(libs.androidx.browser)
  implementation(libs.androidx.core.ktx)
  implementation(libs.coil.kt)
  implementation(libs.coil.kt.compose)
  implementation(libs.kotlinx.datetime)

  api(libs.androidx.compose.foundation)
  api(libs.androidx.compose.foundation.layout)
  api(libs.androidx.compose.material.iconsExtended)
  api(libs.androidx.compose.material3)
  debugApi(libs.androidx.compose.ui.tooling)
  api(libs.androidx.compose.ui.tooling.preview)
  api(libs.androidx.compose.ui.util)
  api(libs.androidx.compose.runtime)
  api(libs.androidx.compose.runtime.livedata)
  api(libs.androidx.metrics)
  api(libs.androidx.tracing.ktx)
}
