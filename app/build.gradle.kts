plugins {
  id("daylighter.android.application")
  id("daylighter.android.application.compose")
  id("daylighter.android.hilt")
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
    named("release") {
      isMinifyEnabled = false
      setProguardFiles(
        listOf(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
      )
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
