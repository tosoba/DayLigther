plugins {
  id("daylighter.android.feature")
  id("daylighter.android.library.compose")
}

android { namespace = "com.trm.daylighter.feature.widgets" }

dependencies { implementation(libs.kotlinx.datetime) }
