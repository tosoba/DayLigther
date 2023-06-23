plugins {
  id("kotlin")
  id("kotlinx-serialization")
}

dependencies {
  implementation(libs.commons.codec)
  implementation(libs.kotlinx.coroutines.android)
  implementation(libs.kotlinx.serialization.json)

  implementation(libs.dagger.dagger)

  testImplementation(libs.junit4)
}
