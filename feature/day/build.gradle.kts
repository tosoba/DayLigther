plugins {
  id("daylighter.android.feature")
  id("daylighter.android.library.compose")
}

android { namespace = "com.trm.daylighter.feature.day" }

dependencies {
  implementation(libs.accompanist.pager)
  implementation(libs.accompanist.pagerindicators)
  implementation(libs.androidx.constraintlayout)

  implementation(libs.osmdroid)

  testImplementation(libs.kotlinx.coroutines.test)
  testImplementation("io.mockk:mockk:1.13.5")
  testImplementation("app.cash.turbine:turbine:1.0.0")
}
