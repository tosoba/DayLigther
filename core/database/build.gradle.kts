plugins {
  id("daylighter.android.library")
  id("daylighter.android.hilt")
  alias(libs.plugins.ksp)
}

android {
  namespace = "com.trm.daylighter.core.database"
  defaultConfig {
    testInstrumentationRunner = "com.trm.daylighter.core.testing.runner.DayLighterTestRunner"
  }
  packaging {
    resources {
      excludes += "/META-INF/{AL2.0,LGPL2.1}"
      excludes += "META-INF/DEPENDENCIES"
    }
  }
}

dependencies {
  implementation(libs.kotlinx.coroutines.android)

  implementation(libs.room.runtime)
  implementation(libs.room.ktx)
  ksp(libs.room.compiler)

  androidTestImplementation(project(":core:testing"))
}
