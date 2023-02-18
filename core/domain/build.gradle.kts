plugins {
  id("kotlin")
  id("kotlinx-serialization")
}

dependencies {
  implementation(libs.kotlinx.coroutines.android)
  implementation(libs.kotlinx.serialization.json)

  implementation(libs.dagger.dagger)
}
