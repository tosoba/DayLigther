import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins { `kotlin-dsl` }

group = "com.trm.daylighter"

// Configure the build-logic plugins to target JDK 17
// This matches the JDK used to build the project, and is not related to what is running on device.
java {
  sourceCompatibility = JavaVersion.VERSION_17
  targetCompatibility = JavaVersion.VERSION_17
}

tasks.withType<KotlinCompile>().configureEach {
  kotlinOptions { jvmTarget = JavaVersion.VERSION_17.toString() }
}

dependencies {
  compileOnly(libs.android.gradlePlugin)
  compileOnly(libs.kotlin.gradlePlugin)
}

tasks {
  validatePlugins {
    enableStricterValidation = true
    failOnWarning = true
  }
}

gradlePlugin {
  plugins {
    register("androidApplicationCompose") {
      id = "daylighter.android.application.compose"
      implementationClass = "AndroidApplicationComposeConventionPlugin"
    }
    register("androidApplication") {
      id = "daylighter.android.application"
      implementationClass = "AndroidApplicationConventionPlugin"
    }
    register("androidApplicationJacoco") {
      id = "daylighter.android.application.jacoco"
      implementationClass = "AndroidApplicationJacocoConventionPlugin"
    }
    register("androidLibraryCompose") {
      id = "daylighter.android.library.compose"
      implementationClass = "AndroidLibraryComposeConventionPlugin"
    }
    register("androidLibrary") {
      id = "daylighter.android.library"
      implementationClass = "AndroidLibraryConventionPlugin"
    }
    register("androidFeature") {
      id = "daylighter.android.feature"
      implementationClass = "AndroidFeatureConventionPlugin"
    }
    register("androidLibraryJacoco") {
      id = "daylighter.android.library.jacoco"
      implementationClass = "AndroidLibraryJacocoConventionPlugin"
    }
    register("androidTest") {
      id = "daylighter.android.test"
      implementationClass = "AndroidTestConventionPlugin"
    }
    register("androidHilt") {
      id = "daylighter.android.hilt"
      implementationClass = "AndroidHiltConventionPlugin"
    }
  }
}
