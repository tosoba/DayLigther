import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins { `kotlin-dsl` }

group = "com.trm.daylighter"

java {
  sourceCompatibility = JavaVersion.VERSION_21
  targetCompatibility = JavaVersion.VERSION_21
}

tasks.withType<KotlinCompile>().configureEach {
  kotlin { compilerOptions { jvmTarget = JvmTarget.JVM_21 } }
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
