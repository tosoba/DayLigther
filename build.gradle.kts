buildscript {
  repositories {
    google()
    mavenCentral()
  }

  dependencies { classpath(libs.android.gradlePlugin) }
}

plugins {
  alias(libs.plugins.android.application) apply false
  alias(libs.plugins.kotlin.jvm) apply false
  alias(libs.plugins.kotlin.serialization) apply false
  alias(libs.plugins.hilt) apply false
  alias(libs.plugins.secrets) apply false
  alias(libs.plugins.ksp) apply false
  id("com.android.library") version "8.2.0" apply false
  id("org.jetbrains.kotlin.android") version "1.9.10" apply false
}
