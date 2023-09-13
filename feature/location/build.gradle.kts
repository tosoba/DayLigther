plugins {
  id("daylighter.android.feature")
  id("daylighter.android.library.compose")
  id("kotlin-parcelize")
}

android { namespace = "com.trm.daylighter.feature.location" }

dependencies {
  implementation(libs.androidx.compose.material)

  implementation(libs.osmdroid)
  implementation(libs.timber)
  implementation(libs.play.services.location)

  debugImplementation(project(":ui-test-hilt-manifest"))
  debugImplementation(libs.androidx.compose.ui.testManifest)

  testImplementation(project(":core:testing"))
  testImplementation(libs.kotlinx.coroutines.test)
  testImplementation(libs.mockk)
  testImplementation(libs.turbine)

  androidTestImplementation(project(":core:testing"))
  androidTestImplementation(libs.mockk.android)
  androidTestImplementation(libs.androidx.compose.ui.test)
  androidTestImplementation(libs.hilt.android.testing)
}
