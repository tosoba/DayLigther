plugins {
  id("daylighter.android.library")
  id("daylighter.android.library.jacoco")
  id("daylighter.android.hilt")
  id("kotlinx-serialization")
  alias(libs.plugins.ksp)
}

android {
  android { namespace = "com.trm.daylighter.core.data" }

  testOptions { unitTests { isIncludeAndroidResources = true } }
}

dependencies {
  implementation(project(":core:common"))
  implementation(project(":core:database"))
  implementation(project(":core:domain"))
  implementation(project(":core:network"))

  implementation(libs.androidx.core.ktx)
  implementation(libs.androidx.work.ktx)

  implementation(libs.room.runtime)
  implementation(libs.room.ktx)
  ksp(libs.room.compiler)

  implementation(libs.kotlinx.coroutines.android)
  implementation(libs.kotlinx.serialization.json)

  implementation("net.iakovlev:timeshape:2022f.15") {
    exclude(group = "com.github.luben", module = "zstd-jni")
  }
  implementation("com.github.luben:zstd-jni:1.5.2-3@aar")
}
