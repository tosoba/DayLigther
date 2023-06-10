plugins {
  id("daylighter.android.feature")
  id("daylighter.android.library.compose")
}

android { namespace = "com.trm.daylighter.feature.settings" }

dependencies {
  implementation(project(":core:datastore"))

  implementation(libs.androidx.dataStore.core)
  implementation(libs.androidx.dataStore.preferences)
  implementation(libs.timber)

  implementation("com.github.JamalMulla:ComposePrefs3:1.0.3")
}
