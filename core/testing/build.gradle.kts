plugins {
  id("daylighter.android.library")
  id("daylighter.android.library.compose")
  id("daylighter.android.hilt")
  alias(libs.plugins.ksp)
}

android { namespace = "com.trm.daylighter.core.testing" }

dependencies {
  implementation(project(":core:common"))
  implementation(project(":core:data"))
  implementation(project(":core:database"))
  implementation(project(":core:domain"))
  implementation(project(":core:ui"))

  implementation(libs.room.runtime)
  implementation(libs.room.ktx)
  ksp(libs.room.compiler)

  api(libs.junit4)
  api(libs.androidx.test.core)
  api(libs.kotlinx.coroutines.test)
  api(libs.turbine)

  api(libs.androidx.test.espresso.core)
  api(libs.androidx.test.runner)
  api(libs.androidx.test.rules)
  api(libs.androidx.compose.ui.test)
  api(libs.hilt.android.testing)
  api(libs.accompanist.testharness)

  debugApi(libs.androidx.compose.ui.testManifest)
}
