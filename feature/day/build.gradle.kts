plugins {
  id("daylighter.android.feature")
  id("daylighter.android.library.compose")
}

android {
  namespace = "com.trm.daylighter.feature.day"
  packaging { resources { excludes += "META-INF/*" } }
}

dependencies {
  implementation(libs.androidx.constraintlayout)
  implementation(libs.androidx.compose.material)

  debugImplementation(project(":ui-test-hilt-manifest"))
  debugImplementation(libs.androidx.compose.ui.testManifest)

  testImplementation(project(":core:testing"))
  testImplementation(libs.kotlinx.coroutines.test)
  testImplementation(libs.mockk)
  testImplementation(libs.turbine)

  androidTestImplementation(project(":core:testing"))
  androidTestImplementation(libs.mockk.android)
  androidTestImplementation(libs.androidx.compose.ui.test)
  androidTestImplementation(libs.hilt.android.testing)
}

tasks.withType<Test>().configureEach {
  jvmArgs("--add-opens", "java.base/java.time=ALL-UNNAMED")
}
