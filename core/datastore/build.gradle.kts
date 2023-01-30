plugins {
  id("daylighter.android.library")
  id("daylighter.android.hilt")
}

android {
  defaultConfig { consumerProguardFiles("consumer-proguard-rules.pro") }
  namespace = "com.trm.daylighter.core.datastore"
}

dependencies {
  implementation(project(":core:common"))
  implementation(project(":core:domain"))

  implementation(libs.kotlinx.coroutines.android)
  implementation(libs.androidx.dataStore.core)
}
