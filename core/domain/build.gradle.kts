plugins {
  id("kotlin")
  id("kotlinx-serialization")
}

dependencies {
  implementation(libs.kotlinx.coroutines.android)
  implementation(libs.kotlinx.serialization.json)

  implementation(libs.dagger.dagger)

  implementation("commons-codec:commons-codec:1.15")

  testImplementation(libs.junit4)
}
