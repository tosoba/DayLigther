plugins {
  id("daylighter.android.feature")
  id("daylighter.android.library.compose")
}

android { namespace = "com.trm.daylighter.feature.about" }

dependencies {
  implementation(project(":core:common"))
  implementation(project(":core:ui"))
}
