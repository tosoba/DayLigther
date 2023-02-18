plugins {
  id("daylighter.android.library")
  id("daylighter.android.hilt")
  id("daylighter.android.library.compose")
}

android { namespace = "com.trm.daylighter.widget" }

dependencies {
  implementation(project(":core:common"))
  implementation(project(":core:data"))
  implementation(project(":core:domain"))
  implementation(project(":core:ui"))

  implementation(libs.glance)
  implementation(libs.kotlinx.coroutines.android)
}
