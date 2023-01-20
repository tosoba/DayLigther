import com.google.protobuf.gradle.builtins
import com.google.protobuf.gradle.generateProtoTasks
import com.google.protobuf.gradle.protobuf
import com.google.protobuf.gradle.protoc

@Suppress("DSL_SCOPE_VIOLATION")
plugins {
  id("daylighter.android.library")
  id("daylighter.android.hilt")
  alias(libs.plugins.protobuf)
}

android {
  defaultConfig { consumerProguardFiles("consumer-proguard-rules.pro") }
  namespace = "com.trm.daylighter.core.datastore"
}

protobuf {
  protoc { artifact = libs.protobuf.protoc.get().toString() }
  generateProtoTasks {
    all().forEach { task ->
      task.builtins {
        val java by registering { option("lite") }
        val kotlin by registering { option("lite") }
      }
    }
  }
}

dependencies {
  implementation(project(":core:common"))
  implementation(project(":core:domain"))

  implementation(libs.kotlinx.coroutines.android)
  implementation(libs.androidx.dataStore.core)
  implementation(libs.protobuf.kotlin.lite)
}
