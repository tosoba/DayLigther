plugins {
  id("daylighter.android.library")
  id("daylighter.android.library.jacoco")
  id("daylighter.android.hilt")
}

android { namespace = "com.trm.daylighter.core.common" }

dependencies {
  implementation(project(":core:domain"))

  implementation(libs.kotlinx.coroutines.android)
  implementation(libs.kotlinx.coroutines.play.services)

  implementation(libs.osmdroid)
  implementation(libs.play.services.location)
  implementation(libs.timber)
}
