plugins {
  id("daylighter.android.library")
  id("daylighter.android.hilt")
}

android {
  namespace = "com.trm.daylighter.sync"
}

dependencies {
  implementation(project(":core:common"))
  implementation(project(":core:data"))
  implementation(project(":core:domain"))

  implementation(libs.kotlinx.coroutines.android)

  implementation(libs.androidx.lifecycle.livedata.ktx)
  implementation(libs.androidx.tracing.ktx)
  implementation(libs.androidx.startup)
  implementation(libs.androidx.work.ktx)

  implementation(libs.hilt.ext.work)
  kapt(libs.hilt.ext.compiler)

  androidTestImplementation(libs.androidx.work.testing)
}
