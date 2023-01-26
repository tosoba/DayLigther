plugins {
  id("kotlin")
  id("kotlin-kapt")
}

dependencies {
  implementation(libs.kotlinx.datetime)
  implementation(libs.kotlinx.coroutines.android)

  implementation(libs.dagger.dagger)
  kapt(libs.dagger.compiler)
}
