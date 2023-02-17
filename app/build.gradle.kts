plugins {
  id("daylighter.android.application")
  id("daylighter.android.application.compose")
  id("daylighter.android.hilt")
}

android {
  namespace = "com.trm.daylighter"
  compileSdk = 33

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
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
  }

  kotlinOptions { jvmTarget = "1.8" }

  buildFeatures { compose = true }

  composeOptions { kotlinCompilerExtensionVersion = "1.3.2" }

  packagingOptions { resources { excludes += "/META-INF/{AL2.0,LGPL2.1}" } }
}

dependencies {
  implementation(project(":core:common"))
  implementation(project(":core:data"))
  implementation(project(":core:datastore"))
  implementation(project(":core:domain"))
  implementation(project(":core:ui"))

  implementation(project(":feature:about"))
  implementation(project(":feature:day"))
  implementation(project(":feature:intro"))
  implementation(project(":feature:location"))
  implementation(project(":feature:locations"))
  implementation(project(":feature:widget"))
  implementation(project(":work"))

  implementation(libs.accompanist.systemuicontroller)
  implementation(libs.androidx.activity.compose)
  implementation(libs.androidx.appcompat)
  implementation(libs.androidx.core.ktx)
  implementation(libs.androidx.core.splashscreen)
  implementation(libs.androidx.compose.runtime)
  implementation(libs.androidx.lifecycle.runtimeCompose)
  implementation(libs.androidx.compose.runtime.tracing)
  implementation(libs.androidx.compose.material3.windowSizeClass)
  implementation(libs.androidx.hilt.navigation.compose)
  implementation(libs.androidx.navigation.compose)
  implementation(libs.androidx.window.manager)
  implementation(libs.androidx.profileinstaller)

  implementation(libs.osmdroid)
}
