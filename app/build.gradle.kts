import com.trm.daylighter.DayLighterBuildType

plugins {
  id("daylighter.android.application")
  id("daylighter.android.application.compose")
  id("daylighter.android.hilt")
  alias(libs.plugins.baselineprofile)
}

android {
  namespace = "com.trm.daylighter"
  compileSdk = 34

  defaultConfig {
    applicationId = "com.trm.daylighter"
    versionCode = 1
    versionName = "1.0"

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    vectorDrawables { useSupportLibrary = true }
  }

  buildTypes {
    debug { applicationIdSuffix = DayLighterBuildType.DEBUG.applicationIdSuffix }
    val release =
      getByName("release") {
        isMinifyEnabled = true
        applicationIdSuffix = DayLighterBuildType.RELEASE.applicationIdSuffix
        proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")

        // To publish on the Play store a private signing key is required, but to allow anyone
        // who clones the code to sign and run the release variant, use the debug signing key.
        // TODO: Abstract the signing configuration to a separate file to avoid hardcoding this.
        signingConfig = signingConfigs.getByName("debug")
        // Ensure Baseline Profile is fresh for release builds.
        baselineProfile.automaticGenerationDuringBuild = true
      }
    create("benchmark") {
      // Enable all the optimizations from release build through initWith(release).
      initWith(release)
      matchingFallbacks.add("release")
      // Debug key signing is available to everyone.
      signingConfig = signingConfigs.getByName("debug")
      // Only use benchmark proguard rules
      proguardFiles("benchmark-rules.pro")
      isMinifyEnabled = true
      applicationIdSuffix = DayLighterBuildType.BENCHMARK.applicationIdSuffix
    }
  }

  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
  }

  buildFeatures { compose = true }

  composeOptions { kotlinCompilerExtensionVersion = libs.versions.androidxComposeCompiler.get() }

  packaging {
    resources {
      excludes += "/META-INF/{AL2.0,LGPL2.1}"
      excludes += "META-INF/DEPENDENCIES"
    }
  }
}

dependencies {
//  baselineProfile(project(":benchmarks"))

  implementation(project(":core:common"))
  implementation(project(":core:data"))
  implementation(project(":core:datastore"))
  implementation(project(":core:domain"))
  implementation(project(":core:ui"))

  implementation(project(":feature:about"))
  implementation(project(":feature:day"))
  implementation(project(":feature:location"))
  implementation(project(":feature:locations"))
  implementation(project(":feature:settings"))
  implementation(project(":feature:widget-location"))
  implementation(project(":widget"))
  implementation(project(":work"))

  implementation(libs.androidx.activity.compose)
  implementation(libs.androidx.appcompat)
  implementation(libs.androidx.core.ktx)
  implementation(libs.androidx.core.splashscreen)
  implementation(libs.androidx.compose.runtime)
  implementation(libs.androidx.lifecycle.runtimeCompose)
  implementation(libs.androidx.compose.runtime.tracing)
  implementation(libs.androidx.hilt.navigation.compose)
  implementation(libs.androidx.navigation.compose)
  implementation(libs.androidx.window.manager)
  implementation(libs.androidx.profileinstaller)

  implementation(libs.osmdroid)
  implementation(libs.timber)
}
