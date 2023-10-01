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
  id("com.android.library") version "7.4.2" apply false
  id("org.jetbrains.kotlin.android") version "1.7.20" apply false
}
