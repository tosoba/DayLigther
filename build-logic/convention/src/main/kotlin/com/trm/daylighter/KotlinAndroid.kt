package com.trm.daylighter

import com.android.build.api.dsl.CommonExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.provideDelegate
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

/** Configure base Kotlin with Android options */
internal fun Project.configureKotlinAndroid(commonExtension: CommonExtension<*, *, *, *, *, *>) {
  commonExtension.apply {
    compileSdk = 36

    defaultConfig { minSdk = 26 }

    compileOptions {
      sourceCompatibility = JavaVersion.VERSION_21
      targetCompatibility = JavaVersion.VERSION_21
      isCoreLibraryDesugaringEnabled = true
    }
  }

  // Treat all Kotlin warnings as errors (disabled by default)
  // Override by setting warningsAsErrors=true in your ~/.gradle/gradle.properties
  val warningsAsErrors: String? by project

  tasks.withType<KotlinCompile>().configureEach {
    compilerOptions {
      allWarningsAsErrors.set(warningsAsErrors.toBoolean())

      freeCompilerArgs.addAll(
        "-opt-in=kotlin.RequiresOptIn",
        // Enable experimental coroutines APIs, including Flow
        "-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi",
        "-opt-in=kotlinx.coroutines.FlowPreview",
        "-opt-in=kotlin.Experimental",
      )

      jvmTarget.set(JvmTarget.JVM_21)
    }
  }

  val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")

  dependencies { add("coreLibraryDesugaring", libs.findLibrary("android.desugarJdkLibs").get()) }
}
