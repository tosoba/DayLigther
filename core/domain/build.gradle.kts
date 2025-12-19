plugins {
  id("kotlin")
  id("kotlinx-serialization")
}

kotlin { compilerOptions { freeCompilerArgs.add("-Xannotation-default-target=param-property") } }

dependencies {
  implementation(libs.commons.codec)
  implementation(libs.kotlinx.coroutines.android)
  implementation(libs.kotlinx.serialization.json)

  implementation(libs.dagger.dagger)

  testImplementation(libs.junit4)
}
