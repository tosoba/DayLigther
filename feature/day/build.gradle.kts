plugins {
  id("daylighter.android.feature")
  id("daylighter.android.library.compose")
}

android {
  namespace = "com.trm.daylighter.feature.day"
  packagingOptions { resources { excludes += "META-INF/*" } }
}

dependencies {
  implementation(libs.accompanist.pager)
  implementation(libs.accompanist.pagerindicators)
  implementation(libs.androidx.constraintlayout)

  implementation(libs.osmdroid)

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
