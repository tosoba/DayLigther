plugins {
  id("daylighter.android.feature")
  id("daylighter.android.library.compose")
}

android { namespace = "com.trm.daylighter.feature.widget.location" }

dependencies {
  implementation(project(":core:common"))
}
