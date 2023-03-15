plugins {
  id("daylighter.android.feature")
  id("daylighter.android.library.compose")
  id("kotlin-parcelize")
}

android { namespace = "com.trm.daylighter.feature.location" }

dependencies {
  implementation(libs.osmdroid)
  implementation(libs.timber)
  implementation("com.google.android.gms:play-services-location:21.0.1")
}
