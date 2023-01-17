plugins {
  id("daylighter.android.library")
  id("daylighter.android.library.jacoco")
  id("daylighter.android.hilt")
  id("kotlinx-serialization")
}

android {
  android { namespace = "com.trm.daylighter.core.common" }

  testOptions { unitTests { isIncludeAndroidResources = true } }
}

dependencies {
  implementation(project(":core:common"))
  implementation(project(":core:database"))
  implementation(project(":core:network"))

  implementation(libs.androidx.core.ktx)

  implementation(libs.kotlinx.datetime)
  implementation(libs.kotlinx.coroutines.android)
  implementation(libs.kotlinx.serialization.json)
}
