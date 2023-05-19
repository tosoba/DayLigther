plugins {
  id("daylighter.android.library")
  id("daylighter.android.hilt")
  id("daylighter.android.library.compose")
  id("kotlinx-serialization")
}

android { namespace = "com.trm.daylighter.widget" }

dependencies {
  implementation(project(":core:common"))
  implementation(project(":core:data"))
  implementation(project(":core:domain"))
  implementation(project(":core:ui"))
  implementation(project(":work"))

  implementation(libs.material)
  implementation(libs.glance.appwidget)
  implementation(libs.glance.material3)

  implementation(libs.kotlinx.coroutines.android)
  implementation(libs.kotlinx.serialization.json)

  implementation(libs.androidx.work.ktx)

  implementation(libs.hilt.ext.work)
  kapt(libs.hilt.ext.compiler)
}
