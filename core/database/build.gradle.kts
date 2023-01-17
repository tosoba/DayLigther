plugins {
  id("daylighter.android.library")
  id("daylighter.android.hilt")
}

android { namespace = "com.trm.daylighter.core.database" }

dependencies {
  implementation(libs.kotlinx.coroutines.android)
  implementation(libs.kotlinx.datetime)
}
