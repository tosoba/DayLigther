plugins {
  id("daylighter.android.library")
  id("daylighter.android.hilt")
  id("kotlinx-serialization")
}

android {
  android { namespace = "com.trm.daylighter.core.network" }

  buildFeatures { buildConfig = true }

  packaging { resources { excludes += "META-INF/DEPENDENCIES" } }
}

dependencies {
  implementation(project(":core:common"))

  implementation(libs.kotlinx.coroutines.android)
  implementation(libs.kotlinx.serialization.json)

  implementation(libs.okhttp.logging)
  implementation(libs.retrofit.core)
  implementation(libs.retrofit.kotlin.serialization)

  api(libs.junit4)
  api(libs.kotlinx.coroutines.test)

  testImplementation(libs.dagger.dagger)
  kspTest(libs.dagger.compiler)
}
