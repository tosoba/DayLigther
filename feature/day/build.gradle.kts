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
}
