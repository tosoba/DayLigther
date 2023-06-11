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
}
