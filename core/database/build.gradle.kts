plugins {
  id("daylighter.android.library")
  id("daylighter.android.hilt")
  alias(libs.plugins.ksp)
}

android { namespace = "com.trm.daylighter.core.database" }

dependencies {
  implementation(libs.kotlinx.coroutines.android)
  implementation(libs.kotlinx.datetime)

  implementation(libs.room.runtime)
  implementation(libs.room.ktx)
  ksp(libs.room.compiler)
}
