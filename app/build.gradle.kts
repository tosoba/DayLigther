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

  composeOptions { kotlinCompilerExtensionVersion = libs.versions.androidxComposeCompiler.get() }

  packagingOptions {
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
  implementation(project(":feature:intro"))
  implementation(project(":feature:location"))
  implementation(project(":feature:locations"))
  implementation(project(":feature:widgets"))
  implementation(project(":widget"))
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
  implementation(libs.timber)

  implementation(files("../libs/nominatim-api.jar"))
  implementation("org.apache.httpcomponents:httpclient:4.5.13")
  implementation(libs.guava.listenablefuture.conflict)
}
