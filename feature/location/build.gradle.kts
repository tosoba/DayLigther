plugins {
  id("daylighter.android.feature")
  id("daylighter.android.library.compose")
  id("kotlin-parcelize")
}

android { namespace = "com.trm.daylighter.feature.location" }

dependencies {
  implementation(libs.material)
  implementation(libs.modalsheet)

  implementation(libs.osmdroid)
  implementation(libs.timber)
  implementation(libs.play.services.location)
  implementation(libs.guava.listenablefuture.conflict)
}
