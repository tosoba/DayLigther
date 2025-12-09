plugins {
  id("daylighter.android.library")
  id("daylighter.android.hilt")
  id("daylighter.android.library.compose")
  id("kotlinx-serialization")
}

android {
  namespace = "com.trm.daylighter.widget"
  buildFeatures { buildConfig = true }
}

dependencies {
  implementation(project(":core:common"))
  implementation(project(":core:data"))
  implementation(project(":core:domain"))
  implementation(project(":core:ui"))

  implementation(project(":feature:location"))
  implementation(project(":feature:widget-location"))

  implementation(project(":work"))

  implementation(libs.androidx.activity.compose)
  implementation(libs.androidx.hilt.navigation.compose)
  implementation(libs.androidx.navigation.compose)

  implementation(libs.material)
  implementation(libs.glance.appwidget)
  implementation(libs.glance.material3)

  implementation(libs.kotlinx.coroutines.android)
  implementation(libs.kotlinx.serialization.json)

  implementation(libs.androidx.work.ktx)

  implementation(libs.hilt.ext.work)
  ksp(libs.hilt.ext.compiler)

  implementation(libs.timber)
}
