plugins {
  id("daylighter.android.feature")
  id("daylighter.android.library.compose")
}

android { namespace = "com.trm.daylighter.feature.settings" }

dependencies {
  implementation(project(":core:common"))
  implementation(project(":core:datastore"))
  implementation(project(":core:ui"))

  implementation(libs.androidx.dataStore.core)
  implementation(libs.androidx.dataStore.preferences)
  implementation(libs.timber)

  implementation(libs.compose.prefs)
}
